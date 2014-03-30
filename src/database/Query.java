package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Query {
	
	public static final String SQL_STATEMENT1 = "select * from files";
	public static final String SQL_STATEMENT2 = "select * from relations";
	public static final String SQL_STATEMENT3 = "select * from tags";
	
	public static final String SQL_GETPATH = "SELECT path FROM files WHERE file_ID = %s";
	public static final String SQL_SETPATH = "UPDATE files SET path = '%s' WHERE file_ID = %s";
	public static final String SQL_ADDPATH = "INSERT INTO files(path) VALUES('%s')";
	public static final String SQL_REMPATH = "DELETE FROM files WHERE file_ID = %s";

	public static final String SQL_GETKW = "SELECT A.value FROM tags_ascii T, ascii A"
			+ "WHERE T.file_ID = %s AND T.tag_ID = %s AND T.val_ID = A.val_ID";
	public static final String SQL_ADDKW = "INSERT INTO tags_ascii VALUES(%s, %s, %s)";
	public static final String SQL_REMKW = "DELETE FROM tags_ascii WHERE file_ID = %s AND tag_ID = %s";

	private static final String SQL_GETASCIIID = "SELECT val_ID FROM ascii WHERE value = '%s'";
	private static final String SQL_ADDASCII = "INSERT INTO ascii VALUES('%s')";
	public static final String SQL_SETASCII = "UPDATE ascii SET value = '%s' WHERE value = '%s'";
	private static final String SQL_DELASCII = "DELETE FROM ascii WHERE value = %s";
	
	private static final int XPKEYWORDS_ID = 0x00009C9E;
	
	//public static final String SQL_GETTAG = "SELECT tag_title FROM tags WHERE tag_ID = %s";
	//public static final String SQL_SETTAG = "UPDATE tags SET tag_title = '%s' WHERE tag_ID = %s";
	//public static final String SQL_ADDTAG = "INSERT INTO tags(tag_title) VALUES('%s')";
	//public static final String SQL_REMTAG = "DELETE FROM tags WHERE tag_ID = %s";
	
	private Connection cnct;
	private Statement  stmt;
	
	public static void main(String[] args) throws SQLException{
		Connection connection = DriverManager.getConnection(CreateDB.JDBC_URL);
		Statement statement = connection.createStatement();
		
		ResultSet resultSetFiles = statement.executeQuery(SQL_STATEMENT1);
		ResultSetMetaData resultSetMetaDataFiles = resultSetFiles.getMetaData();
		printTable(resultSetFiles, resultSetMetaDataFiles);
		
		ResultSet resultSetRelations = statement.executeQuery(SQL_STATEMENT2);
		ResultSetMetaData resultSetMetaDataRelations = resultSetRelations.getMetaData();
		printTable(resultSetRelations, resultSetMetaDataRelations);
		
		ResultSet resultSetTags = statement.executeQuery(SQL_STATEMENT3);
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
		try { return stmt.executeQuery( String.format( SQL_GETKW, fileId, XPKEYWORDS_ID )); }
		catch( SQLException e ) { e.printStackTrace(); return null; }
	}
	
	public void addKeywords( int fileId, String kw ) {
		int valId = formatQueryInt( SQL_GETASCIIID, kw );
		try {
			if( valId == -1 ) {
				formatUpdate( SQL_ADDASCII, kw );
				valId = formatQueryInt( SQL_GETASCIIID, kw );
			}
			formatUpdate( SQL_ADDKW, fileId, XPKEYWORDS_ID, valId );
		} catch( SQLException e ) { e.printStackTrace(); }
	}
	
	public void remKeywords( int fileId ) {
		try { formatUpdate( SQL_REMKW, fileId, XPKEYWORDS_ID ); }
		// if #ofReferences to ascii entry drops to 0: delete?
		catch( SQLException e ) { e.printStackTrace(); }
	}
	
	public void setAscii( String kwold, String kwnew ) {
		try { formatUpdate( SQL_SETASCII, kwnew, kwold ); }
		catch( SQLException e ) { e.printStackTrace(); }
	}
	
	public void delAscii( String str ) {
		try { formatUpdate( SQL_DELASCII, str ); }
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
