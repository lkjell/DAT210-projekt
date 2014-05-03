package database;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import server.FileMetadataUtil;

/**
 * @author Andreas, Joakim
 *
 */
public class Query {

	// Table: files [ file_id, path ]
	private static final String SQL_GET_ALL_FILE_ID = "SELECT file_id FROM files";
	private static final String SQL_GET_PATH = "SELECT path FROM files WHERE file_id = ?";
	private static final String SQL_GET_FILE = "SELECT * FROM files WHERE file_id = ?";
	private static final String SQL_SELECT_PATH = "SELECT * FROM files WHERE files.path = ?";
	private static final String SQL_ADD_FILE = "INSERT INTO files(path,width,height) VALUES(?,?,?)";
	private static final String SQL_SET_PATH = "UPDATE files SET path = ? WHERE file_ID = ?";
	private static final String SQL_REMOVE_PATH = "DELETE FROM files WHERE file_ID = ?";

	// Table: relation [ file_id, xp_tag_id ]
	private static final String SQL_GET_RELATION = "SELECT * from relation WHERE file_id = ? AND xp_tag_id = ?";
	private static final String SQL_ADD_RELATION = "INSERT INTO relation(file_id, xp_tag_id) VALUES(?,?)";
	private static final String SQL_REMKW = "DELETE FROM relation WHERE file_id = ? "
			+ "AND xp_tag_id = (SELECT xp_tag_id FROM xp_tag WHERE tag = ?)";
	
	// Table: xp_tag [ xp_tag_id, tag ]
	private static final String SQL_GET_KEYWORDS = "SELECT xp_tag.tag FROM relation, xp_tag WHERE relation.file_id = ? AND relation.xp_tag_id = xp_tag.xp_tag_id";
	private static final String SQL_GET_KEYWORD = "SELECT * FROM xp_tag WHERE tag = ?";
	private static final String SQL_ADD_KEYWORD = "INSERT INTO xp_tag(tag) VALUES(?)";
	private static final String SQL_DELETE_KEYWORD = "DELETE FROM xp_tag WHERE tag = ?";

	private static final String SELECT_ALL_FILES = "select * from files";
	private static final String SELECT_ALL_RELATIONS = "select * from relation";
	private static final String SELECT_ALL_TAGS = "select * from xp_tag";

//	private static final int XPKEYWORDS_ID = 0x00009C9E;

	public static final String JDBC_URL = "jdbc:derby:MetaDB;create=true";
	private static Logger log = LogManager.getLogger( "Query" );
	private Connection connection;

	//constructor
	public Query() {
		try { connection = DriverManager.getConnection( JDBC_URL ); }
		catch( SQLException e ) { log.error( e, e ); }
	}

	protected void finalize() {
		try { connection.close(); }
		catch( SQLException e ) { log.error( e, e ); }
	}

	/**
	 * Queries the entire MetaDatabase and prints every table with printTable
	 * @throws SQLException if connection was not successfull
	 */
	public void printDatabase() throws SQLException{

		//passer på at databasen ikke commiter slik at resultset kan brukes i hele prosessen
		connection.setAutoCommit(false);

		Statement statement = connection.createStatement();

		//henter alle fra files tabellen, henter kolonne navn og printer
		ResultSet resultSetFiles = statement.executeQuery(SELECT_ALL_FILES);
		printTable(resultSetFiles);

		//henter alle fra relasjons tabellen, henter kolonne navn og printer
		ResultSet resultSetRelations = statement.executeQuery(SELECT_ALL_RELATIONS);
		printTable(resultSetRelations);

		//henter alle fra tag tabellen, henter kolonne navn og printer
		ResultSet resultSetTags = statement.executeQuery(SELECT_ALL_TAGS);
		printTable(resultSetTags);

		//ferdig med transaksjoner
		connection.commit();
		statement.close();
	}

	//printemetode
	/**
	 * Prints a formatted table into System.out
	 * @param table The table to be printed
	 * @throws SQLException If any of the ResultSet operations fail.
	 * @see ResultSet
	 */
	public static void printTable(ResultSet table) throws SQLException{
		ResultSetMetaData tableData = table.getMetaData();
		System.out.println(tableData.getTableName(1));
		int columnCount = tableData.getColumnCount();
		for( int x = 1; x <= columnCount; x++ )
			System.out.format("%100s", table.getMetaData().getColumnName(x) + " | ");

		//looper resultsettet table
		while (table.next()){
			System.out.println("");
			for( int x = 1; x <= columnCount; x++ )
				System.out.format("%100s", table.getString(x) + " | ");
		}
		System.out.println("");
		System.out.println("");
	}

	/**
	 * Queries the database for the path of a file with the fileId
	 * @param fileId The fileId in the database
	 * @return 
	 */
	public File getFile( int fileId ) { return new File( getPath( fileId )); }

	/**
	 *Prompts the database for the full local path to a file.
	 */
	public String getPath( int fileId ) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_GET_PATH );
			return selectString( ps, new Integer( fileId ) );
		} catch( SQLException e ) { log.error( e, e ); return null;
		} finally { closeStatements( ps ); }
	}

	/**
	 * Gets the paths of the files in the database using their file id's.
	 * Only returns for the valid file ID's.
	 * 
	 * @param fileId array of the file Id's of the wanted files in the database
	 * @return String array of the requested paths of the files in the database
	 * @see String
	 */
	public String[] getPaths( int[] fileId ) {
		ArrayList<String> al = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_GET_PATH );
			for( int id : fileId ) al.add( selectString( ps, new Integer( id )));
		} catch (SQLException e) { log.error( e, e ); }
		finally { closeStatements( ps ); }
		return al.toArray(new String[0]);
	}

	/**
	 *Adds files to database and calls addKeywords to extract tags.
	 *Files in subdirectories will be added when directory path is provided.
	 *@return the number of table updates in database
	 *@see addFilesRegex
	 *@see removeFiles
	 *@see update
	 */
	public int addFiles( String... paths ) { return addFiles( true, paths ); }
	public int addFiles( boolean incSubDir, String... paths ) {
		for( String path : paths ) log.info( "Adding files from "+ path );
		int added = 0;
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		try {
			psSelect = connection.prepareStatement( SQL_SELECT_PATH );
			psInsert = connection.prepareStatement( SQL_ADD_FILE, Statement.RETURN_GENERATED_KEYS );
			for( String path : paths ) added += addFiles( psSelect, psInsert, new File( path ), incSubDir );
		} catch (SQLException e) { log.error( e, e ); return added; }
		closeStatements( psSelect, psInsert );
		return added;
	}
 
	/**
	 * @deprecated use addFilesRegex
	 * 
	 * @param ps
	 * @param file
	 * @param subdir
	 * @return
	 * @see addFilesRegex
	 */
	private int addFiles( PreparedStatement psSelect, PreparedStatement psInsert, File file, boolean subdir ) {
		int added = 0;
		if( file.isDirectory() && subdir )
			for( File each : file.listFiles() ) added += addFiles( psSelect, psInsert, each, subdir );
		else if( file.isFile() )
			return addFile( psSelect, psInsert, file, file.getPath() );
		return added;
	}

	/**
	 * 
	 * Adds files to database and calls addKeywords to extract tags.
	 * Files in subdirectories will be added when directory path is provided. 
	 * @param regex	A regular expression string argument to match paths against.
	 * @param paths	The file path/directory
	 * @return The number of table updates in database
	 * @see addFiles
	 * @see addKeywords
	 * @see update
	 */
	public int addFilesRegex( String regex, String... paths) {
		for( String path : paths ) log.info( "Adding files from "+ path );
		int added = 0;
		Pattern pattern = Pattern.compile( regex );
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		try {
			psSelect = connection.prepareStatement( SQL_SELECT_PATH );
			psInsert = connection.prepareStatement( SQL_ADD_FILE, Statement.RETURN_GENERATED_KEYS );
		}
		catch (SQLException e) { log.error( e, e ); return added; }
		for( String path : paths ) added += addFilesRegex( psSelect, psInsert, pattern, new File( path ), true );
		closeStatements( psSelect, psInsert );
		return added;
	}

	private int addFilesRegex( PreparedStatement psSelect, PreparedStatement psInsert, Pattern regex, File file, boolean subdir ) {
		int added = 0;
		if( file.isDirectory() && subdir ) {
			for( File each : file.listFiles() ) added += addFilesRegex( psSelect, psInsert, regex, each, subdir );
		} else if( file.isFile() ) {
			String path = file.getPath();
			if ( regex.matcher( path ).find() ) {
				return addFile( psSelect, psInsert, file, path );
			}
			else log.info( "path did not satisfy regex: "+ path );
		}
		return added;
	}

	/**
	 * Inserts a path into the database. This acts as the file in the database because the actual binary file is not stored in the database.
	 * 
	 * @param ps PreparedStatement object of the connection to the database.
	 * @param path Path to insert into database
	 * @return
	 */
	private int addFile( PreparedStatement psSelect, PreparedStatement psInsert, File file, String path ) {
		int updated = 0;
		BufferedImage img = null;
		FileInputStream is;
		ImageInputStream iis;
		Iterator<ImageReader> i;
		try {
			is = new FileInputStream( file );
			iis = ImageIO.createImageInputStream( is ); }
		catch( IOException e ) { log.error( e, e ); return 0; }
		i = ImageIO.getImageReaders( iis );
		while( i.hasNext() ) {
			ImageReader r = i.next();
			try {
				r.setInput( iis );
				img = r.read( 0 );
				break;
			} catch( IOException e ) { img = null; }
		}
		if( img != null ) {
			try {
				psInsert.setShort( 2, (short) img.getWidth() );
				psInsert.setShort( 3, (short) img.getHeight() );
			} catch( SQLException e ) { log.error( e, e ); return 0; }
		} else {
			log.error( "Could not read image dimensions from: "+ path );
			try {
				psInsert.setNull( 2, Types.SMALLINT );
				psInsert.setNull( 3, Types.SMALLINT );
			} catch ( SQLException e ) { log.error( e, e ); return 0; }
		}
		try {
			psSelect.setString( 1, path );
			psInsert.setString( 1, path );
			int id = insertIfNotExist( psSelect, psInsert );
			String[] keywords = FileMetadataUtil.getXPKeywords( is, path );
			updated += addKeywords( id, keywords );
			log.info( "New file: "+ id +" - "+ path +( keywords.length > 0 ?(
					"\n\tTags: "+ join( "; ", keywords )) : "" ));
		} catch( SQLException e ) { log.error( e, e ); return updated; }
		return updated; // updated rows in db
	}

	/**
	 *Removes all associations with given files or folders in database.
	 *@return the number of table updates in database
	 *@see addFiles
	 *@see addFilesRegex
	 *@see update
	 */
	public int removeFiles( int...fileId ) {
		int removed = 0;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_REMOVE_PATH ,
					PreparedStatement.RETURN_GENERATED_KEYS);
			for( int id : fileId ) {
				ps.setInt( 1, id );
				removed += ps.executeUpdate();
			}
			return removed;
		} catch ( SQLException e ) { log.error( e, e ); return removed; }
		finally { closeStatements( ps ); }
	}

	/**
	 * Updates the path of a file.
	 * @param fileId The id of the file in the database.
	 * @param path The path to replace the old path with.
	 * @see addFiles
	 * @see addFilesRegex
	 * @see removeFiles
	 */
	public void updateFile( int fileId, String path ) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_SET_PATH ,
					PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString( 1, path );
			ps.setInt( 2, fileId );
			ps.executeUpdate();
		} catch ( SQLException e ) { log.error( e, e ); }
		finally { closeStatements( ps ); }
	}

	/**
	 * Gets the id's of all the files in the database.
	 * @return A integer array of all the file id's.
	 */
	public Integer[] getAllFileIds() {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery( SQL_GET_ALL_FILE_ID );
			ArrayList<Integer> al = new ArrayList<>();
			while( rs.next() ) al.add( rs.getInt( 1 ));
			return al.toArray( new Integer[0] );
		} catch ( SQLException e ) {
			log.error( e, e ); 
			return new Integer[0];
		} finally { closeStatements( statement ); }
	}

	/**
	 * Exif:Microsoft:XPKeywords.
	 * Prompts database for all keywords associated with given file ID.
	 * @return the number of table updates in database
	 * @see addKeywords
	 * @see setKeywords
	 * @see removeKeywords
	 */
	public String[] getKeywords( int fileId ) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_GET_KEYWORDS, PreparedStatement.RETURN_GENERATED_KEYS);
			return selectStrings( ps, fileId );
		}
		catch( SQLException e ) {
			log.error( e, e );
			return null;
		} finally { closeStatements( ps ); }
	}

	/**
	 * Adds one or more keywords to specified file and, if the keyword is not in database,
	 * stores the keyword in the keywords table.
	 * @param fileId The ID of the file.
	 * @param keywords The keyword to be added.
	 * @return The amount of updates to the database(
	 * @see getKeywords
	 * @see setKeywords
	 * @see removeKeywords
	 */
	public int addKeywords( int fileId, String...keywords ) {
		int updated = 0;
		if (fileId == 0) return updated;
		PreparedStatement psGetKeyword = null;
		PreparedStatement psAddKeyword = null;
		PreparedStatement psGetRelation = null;
		PreparedStatement psAddRelation = null;
		try { // start try
			psGetKeyword = connection.prepareStatement( SQL_GET_KEYWORD );
			psAddKeyword = connection.prepareStatement( SQL_ADD_KEYWORD, Statement.RETURN_GENERATED_KEYS );
			psGetRelation = connection.prepareStatement( SQL_GET_RELATION );
			psAddRelation = connection.prepareStatement( SQL_ADD_RELATION );
			psGetRelation.setInt( 1, fileId );
			psAddRelation.setInt( 1, fileId );
			for( String kw: keywords ) { // start for
				if ( kw == null ) continue;
				kw = kw.trim();
				if ( kw.isEmpty() ) continue;
				psGetKeyword.setString( 1, kw );
				psAddKeyword.setString( 1, kw );
				int kwId = insertIfNotExist( psGetKeyword, psAddKeyword );
				psGetRelation.setInt( 2, kwId );
				psAddRelation.setInt( 2, kwId );
				insertIfNotExist( psGetRelation, psAddRelation );
			}//end for
		}//end try
		catch ( SQLException e ) {
			log.error( e, e );
		} finally { closeStatements( psGetKeyword, psAddRelation ); }
		return updated;
	}

	/**
	 * Changes the keyword in database. This will affect all files pointing to this keyword.
	 * @return number of rows in database updated
	 * @see addKeywords
	 * @see getKeywords
	 * @see removeKeywords
	 */
	public int setKeyword( String oldkw, String newkw ) {
		PreparedStatement ps = null;
		int updated = 0;
		try {
			ps = connection.prepareStatement( "UPDATE xp_tag SET tag = ? WHERE tag = ?" );
			log.info( "tag '"+ oldkw +"' changing to '"+ newkw +"'" );
			updated = exeUpdate( ps, newkw, oldkw );
			if( updated != 0 ) log.info( "tag '"+ oldkw +"' changed to '"+ newkw +"'" );
		}
		catch ( SQLException e ) { log.error( e, e ); }
		finally { closeStatements( ps ); }
		return updated;
	}
	
	public int removeKeywords( int fileId, String...keywords ) {
		int removed = 0;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_REMKW );
			for( String kw : keywords ) {
				ps.setInt( 1, fileId );
				ps.setString( 2, kw );
				removed += ps.executeUpdate();
				log.debug( "removed tag '"+ kw +"' from file "+ fileId );
			}
//			String kw = join( ";", getKeywords( fileId ));
//			if( FileMetadataUtil.writeXPKeywords( getFile( fileId ), kw ) )
//				log.debug( "removed tags from file. Remaining: "+ kw );
			return removed;
		} catch ( SQLException e ) { log.error( e, e ); return removed; }
		finally { closeStatements( ps ); }
	}
	
	/**
	 * Deletes the specified (one or more) keywords from the database.
	 * @param keywords The keyword to delete
	 * @return The number of database updates as an int.
	 * @see addKeywords
	 * @see getKeywords
	 * @see setKeywords
	 */
 	public int deleteKeywords( String...keywords ) {
		int removed = 0;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_DELETE_KEYWORD ,
					PreparedStatement.RETURN_GENERATED_KEYS );
			for( String kw : keywords ) {
				ps.setString( 1, kw );
				removed += ps.executeUpdate();
				log.debug( "deleted tag '"+ kw +"' from database" );
			}
			return removed;
		} catch ( SQLException e ) { log.error( e, e ); return removed; }
		finally { closeStatements( ps ); }
	}

 	public boolean isSynched( int fileId ) {
 		return false;
 	}
 	
	/**
	 * @return short[] An array or length 2 containing width and height respectively
	 */
	public short[] getDimensions( int fileId ){
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_GET_FILE );
			ps.setInt( 1, fileId );
			ResultSet rs = ps.executeQuery();
			short[] dim = new short[2];
			if( rs.next() ) {
				dim[0] = rs.getShort( 3 );
				dim[1] = rs.getShort( 4 );
			}
			return dim;
		}
		catch( SQLException e ) {
			log.error( e, e );
			return new short[2];
		} finally { closeStatements( ps ); }
	}

	/**
	 * Searches files by comparing filepath and keywords to given words in string
	 * 
	 * @param searchstring words separated by space
	 * @return Integer array of file IDs that matched all words in searchstring
	 */
	public ArrayList<Integer> search( String searchstring ) {
		
		log.info( "searching for: \"" + searchstring + "\"" );
		StringBuilder sb = new StringBuilder();
		if( searchstring == null ) {
			log.debug( "empty search string" );
			Integer[] ints = getAllFileIds();
			ArrayList<Integer> al = new ArrayList<Integer>( ints.length );
			for( Integer i : ints ) al.add( i );
			return al;
		}
		String[] words = searchstring.replace( "\"", "\\\"" ).split( " " ); // don't write \\" in searchstring
		for( int i=0; i<words.length; i++ ) // searches path and keywords for string
				sb.append(( i>0?" AND ":"" ) +"(files.path LIKE '%"+ words[i]
				+ "%' OR (files.file_id = relation.file_id AND relation.xp_tag_id = xp_tag.xp_tag_id "
				+ "AND xp_tag.tag LIKE '%"+ words[i] +"%'))" );
		searchstring = sb.toString();
		PreparedStatement ps = null;
		ArrayList<Integer> al = new ArrayList<>();
		log.info("SELECT DISTINCT files.file_id FROM files, relation, xp_tag WHERE "+ searchstring);
		try {
			ps = connection.prepareStatement(
					"SELECT DISTINCT files.file_id FROM files, relation, xp_tag WHERE "+ searchstring );
			ResultSet rs = ps.executeQuery();
			while( rs.next() ) al.add( rs.getInt( 1 ));
			log.info( "found images: "+ al );
		} catch ( SQLException e ) { log.error( e, e );
		} finally { closeStatements( ps ); }
		return al;
		
	}

	/*@SuppressWarnings("unused")
	private int sqlInsert( String table, Object...objs ) throws SQLException {
		String str = "INSERT " + table + " VALUES(";
		for ( int i=0; i<objs.length; i++ ) {
			str += i!=0?", ":"" + objs[i].toString();
		}
		int a = st.executeUpdate( str );
		connection.commit(); 
		return a;

	}

	@SuppressWarnings("unused")
	private int sqlDelete( String table, Object...objs ) throws SQLException {
		String str = "DELETE FROM " + table + " WHERE ";
		for ( int i=0; i<objs.length; i++ ) {
			str += i!=0 ? " AND " : "" + objs[i].toString();
		}
		int a = st.executeUpdate( str );
		connection.commit();
		return a;
	}*/

	/**
	 * Joins one or more strings into one string with a delimiter.
	 * 
	 * @param delimiter The char that delimits the Strings.
	 * @param objects The strings to join.
	 * @return A string with all the strings delimited by the delimiter.
	 */
	private String join( String delimiter, String...strings ) {
		return join( delimiter, (Object[]) strings );
	}
	private String join( String delimiter, Object...objects ) {
		StringBuilder joined = new StringBuilder();
		boolean first = true;
		for( Object obj : objects ) {
			if( first ) { first = false; }
			else { joined.append( delimiter ); }
			joined.append( obj.toString() );
		}
		return joined.toString();
	}
	
	private int insertIfNotExist( PreparedStatement psSelect, PreparedStatement psInsert ) throws SQLException {
		int id = -1;
		ResultSet rs = psSelect.executeQuery();
		if( rs.next() ) { // if exists in database
			id = rs.getInt( 1 );
		} else {
			rs.close();
			psInsert.executeUpdate();
			rs = psInsert.getGeneratedKeys();
			if ( rs != null && rs.next() ) id = rs.getInt( 1 );
		}
		return id;
	}

	@SuppressWarnings("unused")
	private int selectInt( PreparedStatement ps, Object...args ) throws SQLException {
		ResultSet rs = exeQuery( ps, args );
		rs.next();
		return rs.getInt( 1 );
	}

	@SuppressWarnings("unused")
	private int selectKey(PreparedStatement ps, Object...args ) throws SQLException {
		exeUpdate( ps, args );
		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		return rs.getInt( 1 );
	}

	private String selectString( PreparedStatement ps, Object...args ) throws SQLException {
		ResultSet rs = exeQuery( ps, args );
		rs.next();
		return rs.getString( 1 );
	}

	private String[] selectStrings( PreparedStatement ps, Object...args ) throws SQLException {
		ArrayList<String> al = new ArrayList<>();
		ResultSet rs = exeQuery( ps, args );
		while( rs.next() ) al.add( rs.getString( 1 ));
		return al.toArray(new String[0]);
	}

	private ResultSet exeQuery( PreparedStatement ps, Object...args ) throws SQLException {
		int i = 1;
		for ( Object o : args ) {
			if ( o instanceof Integer ) ps.setInt( i, ((Integer) o).intValue() );
			else if ( o instanceof String ) ps.setString( i, (String) o );
			i++;
		}
		return ps.executeQuery();
	}

	private int exeUpdate( PreparedStatement ps, Object...args ) throws SQLException {
		int i = 1;
		for ( Object o : args ) {
			if ( o instanceof Integer ) ps.setInt( i, ((Integer) o).intValue() );
			else if ( o instanceof String ) ps.setString( i, (String) o );
			i++;
		}
		return ps.executeUpdate();
	}

	private void closeStatements( Statement...statements ) {
		for ( Statement s : statements ) {
			if( s != null )
				try { s.close(); }
				catch( SQLException e ) { log.error( e, e ); }
		}
	}
}
