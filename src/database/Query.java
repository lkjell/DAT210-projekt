package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import server.FileMetadataUtil;

public class Query {

	// Table: files [ file_id, path ]
	private static final String SQL_ALL_FILE_ID =
			"SELECT file_id FROM files";
	private static final String SQL_GET_PATH =
			"SELECT path FROM files WHERE file_id = ?";
	private static final String SQL_ADD_PATH =
			"SELECT * FROM files WHERE files.path = ?";
//			"INSERT INTO files(path) VALUES(?) WHERE NOT EXIST( "
//			+ "SELECT * FROM files WHERE path = ? )";
	private static final String SQL_SET_PATH =
			"UPDATE files SET path = ? WHERE file_ID = ?";
	private static final String SQL_REMOVE_PATH =
			"DELETE FROM files WHERE file_ID = ?";

	// Table: relation [ file_id, xp_tag_id ]
	private static final String SQL_ADD_RELATION =
			"INSERT INTO relation VALUES(?,?)";
	
	// Table: xp_tag [ xp_tag_id, tag ]
	public static final String SQL_GET_KEYWORDS =
			"SELECT xp_tag.tag FROM relation, xp_tag "
			+ "WHERE relation.file_id = ? "
			+ "AND relation.xp_tag_id = xp_tag.xp_tag_id";
	private static final String SQL_ADD_KEYWORDS =
			"INSERT INTO xp_tag(tag) VALUES(?) WHERE NOT EXIST( "
			+ "SELECT * FROM xp_tag WHERE tag = ? )";
	private static final String SQL_REMOVE_KEYWORD =
			"DELETE FROM xp_tag WHERE tag = ?";
	
	private static final String SELECT_ALL_FILES = "select * from files";
	private static final String SELECT_ALL_RELATIONS = "select * from relation";
	private static final String SELECT_ALL_TAGS = "select * from xp_tag";

//	private static final String SQL_GETFILEID = "SELECT file_id FROM files WHERE path = '%s'";
//
//	private static final String SQL_REMKW = "DELETE FROM relation WHERE file_ID = %s AND xp_tag_ID = %s";
//
//	private static final String SQL_GET_XP_TAG_ID = "SELECT xp_tag_ID FROM xp_tag WHERE tag = '%s'";
//	private static final String SQL_ADD_XP_TAG = "INSERT INTO xp_tag (tag) VALUES('%s')";
//	private static final String SQL_SET_XP_TAG = "UPDATE xp_tag SET tag = '%s' WHERE tag = '%s'";
//	private static final String SQL_DELETE_XP_TAG = "DELETE FROM xp_tag WHERE tag = %s";

//	private static final int XPKEYWORDS_ID = 0x00009C9E;
//
//	public static final String SQL_GETTAG = "SELECT tag_title FROM tags WHERE tag_ID = %s";
//	public static final String SQL_SETTAG = "UPDATE tags SET tag_title = '%s' WHERE tag_ID = %s";
//	public static final String SQL_ADDTAG = "INSERT INTO tags(tag_title) VALUES('%s')";
//	public static final String SQL_REMTAG = "DELETE FROM tags WHERE tag_ID = %s";

	public static final String JDBC_URL = "jdbc:derby:MetaDB;create=true";
	private static Logger log = LogManager.getLogger();
	
	private static Connection connection;
	
	//constructor
	public Query() {
		try {
			connection = DriverManager.getConnection(JDBC_URL);
		} catch( SQLException e ) { e.printStackTrace(); }
	}

	//henter hele databasen og printer
	public void printDatabase() throws SQLException{
		
		//passer på at databasen ikke commiter slik at resultset kan brukes i hele prosessen
		connection.setAutoCommit(false);
		
		Statement statement = connection.createStatement();

		//henter alle fra files tabellen, henter kolonne navn og printer
		ResultSet resultSetFiles = statement.executeQuery(SELECT_ALL_FILES);
		ResultSetMetaData resultSetMetaDataFiles = resultSetFiles.getMetaData();
		printTable(resultSetFiles, resultSetMetaDataFiles);

		//henter alle fra relasjons tabellen, henter kolonne navn og printer
		ResultSet resultSetRelations = statement.executeQuery(SELECT_ALL_RELATIONS);
		ResultSetMetaData resultSetMetaDataRelations = resultSetRelations.getMetaData();
		printTable(resultSetRelations, resultSetMetaDataRelations);

		//henter alle fra tag tabellen, henter kolonne navn og printer
		ResultSet resultSetTags = statement.executeQuery(SELECT_ALL_TAGS);
		ResultSetMetaData resultSetMetaDataTags = resultSetTags.getMetaData();
		printTable(resultSetTags, resultSetMetaDataTags);

		//ferdig med transaksjoner
		connection.commit();
		statement.close();
	}

	//printemetode
	public static void printTable(ResultSet table, ResultSetMetaData tableData) throws SQLException{
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

	//henter filepath og lager file object av den
	
	/**
	 *
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
		} catch( SQLException e ) {
			e.printStackTrace( System.out );
			return null;
		} finally {
			try { if( ps != null ) ps.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
	}
	
	/**
	 *
	 */
	public String[] getPaths( int[] fileId ) {
		ArrayList<String> al = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_GET_PATH );
			for( int id : fileId ) al.add( selectString( ps, new Integer( id )));
		} catch (SQLException e) { e.printStackTrace(); }
		finally {
			try { if( ps != null ) ps.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
		return (String[]) al.toArray();
	}
	
	/**
	 *Adds files to database and calls addKeywords to extract tags.
	 *Files in subdirectories will be added when directory path is provided.
	 *@return the number of table updates in database
	 *@see addFilesRegex
	 *@see removeFiles
	 *@see update
	 */
	public int addFiles( String... paths ) {
		for( String path : paths ) log.info( "Adding files from "+ path );
		int added = 0;
		PreparedStatement ps = null;
		try {
			 ps = connection.prepareStatement( SQL_ADD_PATH,
					 Statement.RETURN_GENERATED_KEYS,
					 ResultSet.CONCUR_UPDATABLE,
					 ResultSet.TYPE_SCROLL_SENSITIVE);
			 for( String path : paths ) added += addFiles( ps, new File( path ), true );
		} catch (SQLException e) { e.printStackTrace(); return added; }
		try { if( ps != null ) connection.commit(); ps.close(); }
		catch( SQLException e ) { e.printStackTrace(); }
		return added;
	}
	
	private int addFiles( PreparedStatement ps, File file, boolean subdir ) {
		int added = 0;
		if( file.isDirectory() && subdir )
			for( File each : file.listFiles() ) added += addFiles( ps, each, subdir );
		else if( file.isFile() )
			return addPath( ps, file.getPath() );
		return added;
	}
	
	/**
	 *Adds files to database and calls addKeywords to extract tags.
	 *Takes a regex string argument to match paths against.
	 *Files in subdirectories will be added when directory path is provided.
	 *@return the number of table updates in database
	 *@see addFiles
	 *@see removeFiles
	 *@see update
	 */
	public int addFilesRegex( String regex, String... paths) {
		for( String path : paths ) log.info( "Adding files from "+ path );
		for( String path : paths ) System.out.println( "Adding files from "+ path );
		int added = 0;
		Pattern pattern = Pattern.compile( regex );
		PreparedStatement ps = null;
		try { ps = connection.prepareStatement( SQL_ADD_PATH, Statement.RETURN_GENERATED_KEYS ); }
		catch (SQLException e) { e.printStackTrace(); return added; }
		for( String path : paths ) added += addFilesRegex( ps, pattern, new File( path ), true );
		try { if( ps != null ) ps.close(); }
		catch( SQLException e ) { e.printStackTrace(); }
		return added;
	}
	
	private int addFilesRegex( PreparedStatement ps, Pattern regex, File file, boolean subdir ) {
		int added = 0;
		if( file.isDirectory() && subdir ) {
			for( File each : file.listFiles() ) added += addFilesRegex( ps, regex, each, subdir );
		} else if( file.isFile() ) {
			String path = file.getPath();
			if ( regex.matcher( path ).find() ) return addPath( ps, path );
			else log.warn( "path did not satisfy regex: "+ path );
			System.out.println( "path did not satisfy regex: "+ path );
		}
		return added;
	}

	private int addPath( PreparedStatement ps, String path ) { //SQL injection sensitive!
		int updated = 0;
		try {
			int id = 0;
			ps.setString( 1, path );
			ResultSet rs = ps.executeQuery();
			if ( rs.next() ) {
				id = rs.getInt( 1 );
			} else {
				rs.moveToInsertRow();
				rs.updateString( 2, path );
				rs.insertRow();
				id = rs.getInt( 1 );
				updated++;
			}
			//int id = selectKey( ps, path );
			String[] keywords = FileMetadataUtil.getXPKeywords( path );
			updated += addKeywords( id, keywords );
			log.info( "New file: "+ id +" - "+ path +( keywords.length > 0 ?(
					"\n\tTags: "+ join( "; ", keywords )) : "" ));
			System.out.println( "New file: "+ id +" - "+ path +( keywords.length > 0 ?(
					"\n\tTags: "+ join( "; ", keywords )) : "" ));
		} catch( SQLException e ) { e.printStackTrace(); }
		return updated; // updated rows in db
	}
	
	private String join( String delimiter, String...strings ) {
		StringBuilder joined = new StringBuilder();
		boolean first = true;
		for( String str : strings ) {
			if( first ) {  first = false; }
			else { joined.append( delimiter ); }
			joined.append( str );
		}
		return joined.toString();
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
			ps = connection.prepareStatement( SQL_REMOVE_PATH );
			for( int id : fileId ) {
				ps.setInt( 1, id );
				removed += ps.executeUpdate();
			}
			return removed;
		} catch ( SQLException e ) { e.printStackTrace(); return removed; }
		finally {
			try { if( ps != null ) ps.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
	}

	/**
	 *
	 *@return the number of table updates in database
	 *@see addFiles
	 *@see addFilesRegex
	 *@see removeFiles
	 */
	public void updateFile( int fileId, String value ) { //SQL injection sensitive!
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_SET_PATH );
			ps.setString( 1, value );
			ps.setInt( 2, fileId );
			ps.executeUpdate();
		} catch ( SQLException e ) { e.printStackTrace(); }
		finally {
			try { if( ps != null ) ps.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
	}
	
	/**
	 *
	 */
	public Integer[] getAllFileIds() {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery( SQL_ALL_FILE_ID );
			ArrayList<Integer> al = new ArrayList<>();
			while( rs.next() ) al.add( rs.getInt( 1 ));
			return al.toArray( new Integer[0] );
		} catch ( SQLException e ) {
			e.printStackTrace(); 
			return new Integer[0];
		} finally {
			try { if( statement != null ) statement.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
	}
	
	/**
	 *Exif:Microsoft:XPKeywords.
	 *Prompts database for all keywords associated with given file ID.
	 *@return the number of table updates in database
	 *@see addKeywords
	 *@see removeKeywords
	 */
	public String[] getKeywords( int fileId ) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_GET_KEYWORDS );
			return selectStrings( ps, fileId );
		}
		catch( SQLException e ) {
			e.printStackTrace();
			return null;
		} finally {
			try { if( ps != null ) ps.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
	}
	
	/**
	 *
	 *@see getKeywords
	 *@see removeKeywords
	 */
	public int addKeywords( int fileId, String...keywords ) { //SQL injection sensitive!
		int updated = 0;
		PreparedStatement psKws = null;
		PreparedStatement psRel = null;
		try {
			psKws = connection.prepareStatement( SQL_ADD_KEYWORDS,
					Statement.RETURN_GENERATED_KEYS );
			psRel = connection.prepareStatement( SQL_ADD_RELATION );
			psRel.setInt( 1, fileId);
			for( String kw: keywords ) {
				int kwId = selectKey( psKws, kw, kw );
				psRel.setInt( 2, kwId);
				psRel.executeUpdate();
				updated++;
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		} finally {
			try { if( psKws != null ) psKws.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
			try { if( psRel != null ) psRel.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
		return updated;
	}


	/**
	 *
	 *@see getKeywords
	 *@see addKeywords
	 */
	public int removeKeywords( String...keywords ) { //SQL injection sensitive!
		int removed = 0;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement( SQL_REMOVE_KEYWORD );
			for( String kw : keywords ) {
				ps.setString( 1, kw );
				removed += ps.executeUpdate();
			}
			return removed;
		} catch ( SQLException e ) { e.printStackTrace(); return removed; }
		finally {
			try { if( ps != null ) ps.close(); }
			catch( SQLException e ) { e.printStackTrace(); }
		}
	}
	
	/*public ResultSet search( String conditions ) {

	StringBuilder sb = new StringBuilder();
	String[] s = conditions.replace( "\"", "\\\"" ).split( " " ); // BUG PRONE !!!
	for( int i=0; i<s.length; i++ ) {
		sb.append(( i>0?" AND ":"" ) +"path LIKE \"%"+ s[i] +"%\"" );
	}
	conditions = sb.toString();

	ResultSet result = null;
	try {
		result = stmt.executeQuery( "SELECT * FROM files WHERE "+ conditions +";" );
	} catch( SQLException e ) { e.printStackTrace(System.out); }
	return result;
}*/
	
	/*
	public void setAscii( String kwold, String kwnew ) {
		try { formatUpdate( SQL_SET_XP_TAG, kwnew, kwold ); }
		catch( SQLException e ) { e.printStackTrace(System.out); }
	}

	public void delAscii( String str ) {
		try { formatUpdate( SQL_DELETE_XP_TAG, str ); }
		catch( SQLException e ) { e.printStackTrace(System.out); }
	}
	*/
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

	@SuppressWarnings("unused")
	private int selectInt( PreparedStatement ps, Object...args ) throws SQLException {
		ResultSet rs = exeQuery( ps, args );
		rs.next();
		return rs.getInt( 1 );
	}
	
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
		return (String[]) al.toArray();
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
}
