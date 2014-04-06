package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Query {
	//TODO bruk prepared statement http://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
	
	
	public static final String SELECT_ALL_FILES = "select * from files";
	public static final String SELECT_ALL_RELATIONS = "select * from relation";
	public static final String SELECT_ALL_TAGS = "select * from xp_tag";
	
	public static final String SQL_GETPATH = "SELECT path FROM files WHERE file_ID = %s";
	public static final String SQL_SETPATH = "UPDATE files SET path = '%s' WHERE file_ID = %s";
	public static final String SQL_ADDPATH = "INSERT INTO files(path) VALUES('%s')";
	public static final String SQL_REMPATH = "DELETE FROM files WHERE file_ID = %s";

	public static final String SQL_GETKW = "SELECT xp_tag.tag FROM relation, xp_tag"
			+ "WHERE relation.file_ID = %s  AND relation.xp_tag_ID = xp_tag.xp_tag_ID";
	public static final String SQL_ADDKW = "INSERT INTO relation VALUES(%s, %s)";
	public static final String SQL_REMKW = "DELETE FROM relation WHERE file_ID = %s AND xp_tag_ID = %s";

	private static final String SQL_GET_XP_TAG_ID = "SELECT val_ID FROM xp_tag WHERE tag = '%s'";
	private static final String SQL_ADD_XP_TAG = "INSERT INTO xp_tag VALUES('%s')";
	public static final String SQL_SET_XP_TAG = "UPDATE xp_tag SET tag = '%s' WHERE tag = '%s'";
	private static final String SQL_DELETE_XP_TAG = "DELETE FROM xp_tag WHERE tag = %s";
	
	//private static final int XPKEYWORDS_ID = 0x00009C9E;
	
	//public static final String SQL_GETTAG = "SELECT tag_title FROM tags WHERE tag_ID = %s";
	//public static final String SQL_SETTAG = "UPDATE tags SET tag_title = '%s' WHERE tag_ID = %s";
	//public static final String SQL_ADDTAG = "INSERT INTO tags(tag_title) VALUES('%s')";
	//public static final String SQL_REMTAG = "DELETE FROM tags WHERE tag_ID = %s";
	
	private Connection cnct;
	private Statement  stmt;
	
	public static void main(String[] args) throws SQLException{
		Connection connection = DriverManager.getConnection(CreateDB.JDBC_URL);
		Statement statement = connection.createStatement();
		
		
		ResultSet resultSetFiles = statement.executeQuery(SELECT_ALL_FILES);
		ResultSetMetaData resultSetMetaDataFiles = resultSetFiles.getMetaData();
		printTable(resultSetFiles, resultSetMetaDataFiles);
		
		ResultSet resultSetRelations = statement.executeQuery(SELECT_ALL_RELATIONS);
		ResultSetMetaData resultSetMetaDataRelations = resultSetRelations.getMetaData();
		printTable(resultSetRelations, resultSetMetaDataRelations);
		
		ResultSet resultSetTags = statement.executeQuery(SELECT_ALL_TAGS);
		ResultSetMetaData resultSetMetaDataTags = resultSetTags.getMetaData();
		printTable(resultSetTags, resultSetMetaDataTags);
		
		if (statement != null) statement.close();
		if (connection != null) connection.close();
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
			stmt = cnct.createStatement();
		} catch( SQLException e ) { e.printStackTrace(); }
	}
	
	public File getFile( int fileId ) { return new File( getPath( fileId )); }
	
	public String getPath( int fileId ) {
		try { return stmt.executeQuery( String.format( SQL_GETPATH, fileId )).getString( 1 ); }
		catch( SQLException e ) { e.printStackTrace(); return null; }
	}
	
	public ResultSet search( String conditions ) {
		
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
		try { return formatUpdate( SQL_ADDPATH, path ); }
		catch( SQLException e ) { e.printStackTrace(); return 0; }
	}

	public int removeFiles( int... fileId ) {
		int removed = 0;
		for( int id : fileId ) { removed += removeFile( id ); }
		return removed;
	}
	
	public int removeFile( int fileId ) {
		try { return formatUpdate( SQL_REMPATH, fileId ); }
		catch( SQLException e ) { e.printStackTrace(); return 0; }
	}
	
	public int update( int fileId, String value ) {
		try { return formatUpdate( SQL_SETPATH, value, fileId ); }
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

	public ResultSet getKeywords( int fileId ) {
		try { return stmt.executeQuery( String.format( SQL_GETKW, fileId)); }
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
	
	private int sqlInsert( String table, Object...objs ) throws SQLException {
		String str = "INSERT " + table + " VALUES(";
		for ( int i=0; i<objs.length; i++ ) {
			str += i!=0?", ":"" + objs[i].toString();
		}
		return stmt.executeUpdate( str );
	}
	
	private int sqlDelete( String table, Object...objs ) throws SQLException {
		String str = "DELETE FROM " + table + " WHERE ";
		for ( int i=0; i<objs.length; i++ ) {
			str += i!=0 ? " AND " : "" + objs[i].toString();
		}
		return stmt.executeUpdate( str );
	}
	
	private int formatUpdate( String sql, Object...objs ) throws SQLException {
		return stmt.executeUpdate( String.format( sql, objs ));
	}
	private ResultSet formatQuery( String sql, Object...objs ) throws SQLException {
		return stmt.executeQuery( String.format( sql, objs ));
	}
	private int formatQueryInt( String sql, Object...objs ) {
		try{ return stmt.executeQuery( String.format( sql, objs )).getInt( 1 ); }
		catch( SQLException e ) { return -1; }
	}
}
