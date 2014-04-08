package database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;

public class Query {
	//TODO bruk prepared statement http://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html


	public static final String SELECT_ALL_FILES = "select * from files";
	public static final String SELECT_ALL_RELATIONS = "select * from relation";
	public static final String SELECT_ALL_TAGS = "select * from xp_tag";

	public static final String SQL_GETFILEID = "SELECT file_id FROM files WHERE path = '%s'";
	public static final String SQL_GETPATH = "SELECT path FROM files WHERE file_ID = %s";
	public static final String SQL_SETPATH = "UPDATE files SET path = '%s' WHERE file_ID = %s";
	public static final String SQL_ADDPATH = "INSERT INTO files(path) VALUES('%s')";
	public static final String SQL_REMPATH = "DELETE FROM files WHERE file_ID = %s";

	public static final String SQL_GETKW = "SELECT xp_tag.tag FROM relation, xp_tag"
			+ " WHERE relation.file_ID = %s AND relation.xp_tag_ID = xp_tag.xp_tag_ID";
	public static final String SQL_ADDKW = "INSERT INTO relation VALUES(%s, %s)";
	public static final String SQL_REMKW = "DELETE FROM relation WHERE file_ID = %s AND xp_tag_ID = %s";

	private static final String SQL_GET_XP_TAG_ID = "SELECT xp_tag_ID FROM xp_tag WHERE tag = '%s'";
	private static final String SQL_ADD_XP_TAG = "INSERT INTO xp_tag (tag) VALUES('%s')";
	public static final String SQL_SET_XP_TAG = "UPDATE xp_tag SET tag = '%s' WHERE tag = '%s'";
	private static final String SQL_DELETE_XP_TAG = "DELETE FROM xp_tag WHERE tag = %s";

	//private static final int XPKEYWORDS_ID = 0x00009C9E;

	//public static final String SQL_GETTAG = "SELECT tag_title FROM tags WHERE tag_ID = %s";
	//public static final String SQL_SETTAG = "UPDATE tags SET tag_title = '%s' WHERE tag_ID = %s";
	//public static final String SQL_ADDTAG = "INSERT INTO tags(tag_title) VALUES('%s')";
	//public static final String SQL_REMTAG = "DELETE FROM tags WHERE tag_ID = %s";

	private Connection cnct;
	private Statement  stmt;

	public void printDatabase() throws SQLException{
		Statement statement = cnct.createStatement();

		ResultSet resultSetFiles = statement.executeQuery(SELECT_ALL_FILES);
		ResultSetMetaData resultSetMetaDataFiles = resultSetFiles.getMetaData();
		printTable(resultSetFiles, resultSetMetaDataFiles);

		ResultSet resultSetRelations = statement.executeQuery(SELECT_ALL_RELATIONS);
		ResultSetMetaData resultSetMetaDataRelations = resultSetRelations.getMetaData();
		printTable(resultSetRelations, resultSetMetaDataRelations);

		ResultSet resultSetTags = statement.executeQuery(SELECT_ALL_TAGS);
		ResultSetMetaData resultSetMetaDataTags = resultSetTags.getMetaData();
		printTable(resultSetTags, resultSetMetaDataTags);

		cnct.commit();
	}

	public static void printTable(ResultSet table, ResultSetMetaData tableData) throws SQLException{
		System.out.println(tableData.getTableName(1));
		int columnCount = tableData.getColumnCount();
		for( int x = 1; x <= columnCount; x++ )
			System.out.format("%20s", table.getMetaData().getColumnName(x) + " | ");
		while (table.next()){
			System.out.println("");
			for( int x = 1; x <= columnCount; x++ )
				System.out.format("%20s", table.getString(x) + " | ");
		}
		System.out.println("");
		System.out.println("");
	}

	public Query() {
		try {
			cnct = DriverManager.getConnection( CreateDB.JDBC_URL );
			cnct.setAutoCommit(false);
			stmt = cnct.createStatement();
		} catch( SQLException e ) { e.printStackTrace(); }
	}



	public File getFile( int fileId ) { return new File( getPath( fileId )); }

	public String getPath( int fileId ) {
		try { 
			ResultSet rs = stmt.executeQuery( String.format( SQL_GETPATH, fileId ));
			rs.next();
			String ret = rs.getString(1);
			cnct.commit();
			return ret; }
		catch( SQLException e ) { e.printStackTrace(); return null; }
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
		} catch( SQLException e ) { e.printStackTrace(); }
		return result;
	}*/

	public Integer[] getAllFileIds() {
		try {
			ResultSet rs = stmt.executeQuery("SELECT file_id FROM files");
			ArrayList<Integer> faen = new ArrayList<>();
			while(rs.next()) {
				faen.add(rs.getInt(1));
			}
			Integer[] a = faen.toArray(new Integer[1]);
			cnct.commit();
			return a;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
			return new Integer[0];
		}

	}

	public int addFiles( String... paths ) {
		int added = 0;
		for( String path : paths ) added += addFile( new File( path ) );
		return added;
	}

	private int addFile( File file ) {
		int added = 0;
		if( file.isDirectory() ) {
			for( File each : file.listFiles() ) { added += addFile( each ); }
		} else return addPath( file.getPath() );
		return added;
	}

	private int addPath( String path ) {
		int updated = 0;
		int id = 0;
		String[] kws = null;
		try {
			updated = formatUpdate( SQL_ADDPATH, path );
			id = formatQueryInt( SQL_GETFILEID, path );
			final JpegImageMetadata jpeg = (JpegImageMetadata) Imaging.getMetadata( new File( path ));
			if (jpeg == null) return updated;
			final TiffField field = jpeg.findEXIFValueWithExactMatch(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
			if (field == null) return updated;
			kws = field.getStringValue().trim().split( ";" );
			
		} catch( SQLException | ImageReadException | IOException e ) {
			e.printStackTrace();
			return updated;
		}
		try {
			ResultSet rs = formatQuery( SQL_GETPATH, 1 );
			if (rs.next()) { System.out.println( rs.getString( 1 ) ); }
			cnct.commit();
		} catch (SQLException e) {e.printStackTrace();}
		System.out.println( id +" >> "+ path );
		for( String kw: kws ) { addKeywords( id, kw ); System.out.print( kw +"; " ); }
		System.out.println();
		return updated;
	}

	public int removeFiles( int... fileId ) {
		int removed = 0;
		for( int id : fileId ) { removed += removeFile( id ); }
		return removed;
	}

	public int removeFile( int fileId ) {
		try { int ret = formatUpdate( SQL_REMPATH, fileId ); cnct.commit(); return ret; }
		catch( SQLException e ) { e.printStackTrace(); return 0; }
	}

	public int update( int fileId, String value ) {
		try { int ret = formatUpdate( SQL_SETPATH, value, fileId ); cnct.commit(); return ret; }
		catch( SQLException e ) { e.printStackTrace(); return 0; }
	}

	//	public int addTags( int fileId, int tagId) {
	//		int added = 0;
	//		//for( int file : fileid ) added += 0;
	//		return added;
	//	}

	/*
	 * Exif : Microsoft : XPKeywords
	 */

	public String[] getKeywords( int fileId ) {
		try {
			ArrayList<String> arraylist = new ArrayList<>();
			
			ResultSet rs = stmt.executeQuery( String.format( SQL_GETKW, fileId));
			while(rs.next()) arraylist.add(rs.getString(1));
			cnct.commit();
			String[] ret = new String[arraylist.size()];
			arraylist.toArray(ret);
			return ret;
			}
		catch( SQLException e ) { e.printStackTrace(); return null; }
		
	}

	public void addKeywords( int fileId, String kw ) {
		int valId = formatQueryInt( SQL_GET_XP_TAG_ID, kw );
		try {
			if( valId == -1 ) {
				formatUpdate( SQL_ADD_XP_TAG, kw );
				valId = formatQueryInt( SQL_GET_XP_TAG_ID, kw );
			}
			formatUpdate( SQL_ADDKW, fileId, valId );
		} catch( SQLException e ) { e.printStackTrace(); }
	}

	public void remKeywords( int fileId ) {
		try { formatUpdate( SQL_REMKW, fileId); }
		// if #ofReferences to xp_tag entry drops to 0: delete?
		catch( SQLException e ) { e.printStackTrace(); }
	}

	public void setAscii( String kwold, String kwnew ) {
		try { formatUpdate( SQL_SET_XP_TAG, kwnew, kwold ); }
		catch( SQLException e ) { e.printStackTrace(); }
	}

	public void delAscii( String str ) {
		try { formatUpdate( SQL_DELETE_XP_TAG, str ); }
		catch( SQLException e ) { e.printStackTrace(); }
	}

	@SuppressWarnings("unused")
	private int sqlInsert( String table, Object...objs ) throws SQLException {
		String str = "INSERT " + table + " VALUES(";
		for ( int i=0; i<objs.length; i++ ) {
			str += i!=0?", ":"" + objs[i].toString();
		}
		int a = stmt.executeUpdate( str );
		cnct.commit(); 
		return a;

	}

	@SuppressWarnings("unused")
	private int sqlDelete( String table, Object...objs ) throws SQLException {
		String str = "DELETE FROM " + table + " WHERE ";
		for ( int i=0; i<objs.length; i++ ) {
			str += i!=0 ? " AND " : "" + objs[i].toString();
		}
		int a = stmt.executeUpdate( str );
		cnct.commit();
		return a;
	}

	private int formatUpdate( String sql, Object...objs ) throws SQLException {
		int a = stmt.executeUpdate( String.format( sql, objs ));
		cnct.commit();
		return a;
	}
	
	private ResultSet formatQuery( String sql, Object...objs ) throws SQLException {
		ResultSet a = stmt.executeQuery( String.format( sql, objs ));
		return a;
	}
	
	private int formatQueryInt( String sql, Object...objs ) {
		try{ 
			ResultSet satan = stmt.executeQuery( String.format( sql, objs ));
			if(satan.next()){
				int ret = satan.getInt(1); 
				cnct.commit();
				return ret;}else {cnct.commit(); return -1;}
		}
		catch( SQLException e ) { e.printStackTrace();return -1; }
	}

}
