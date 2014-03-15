package database;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
		String str = null;
		try {
			str = stmt.executeQuery( "SELECT path FROM files WHERE file_ID = "+ fileId +";" ).getString( 1 );
			/*if ( str.startsWith( ">" )) { // contains only relative path, get full path
				int dirId = ByteBuffer.wrap( str.getBytes() ).getInt( 1 );
				str = stmt.executeQuery( "SELECT path FROM dirs WHERE dir_ID = "+ dirId +";" ).getString( 1 )
						+ str.substring( 5 );
			}*/
		} catch( SQLException e ) { e.printStackTrace(); }
		return str;
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
	
	public int addFiles( String[] paths ) {
		int added = 0;
		for( String path : paths ) added += addFiles( path );
		return added;
	}
	
	public int addFiles( String path ) {
		URI uri = null;
		try { uri = new URI( path ); }
		catch( URISyntaxException e ) { e.printStackTrace(); return 0; }
		
		System.out.println( "adding file(s) from "+ uri.getPath() );
		System.out.println( "scheme "+ uri.getScheme() );
		
		int added = 0;
		if( uri.getScheme().equals( "file:" )) {
			File file = new File( uri.getPath() );
			added = addFile( file );
		} else return addPath( uri.getPath() );
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
		try { return stmt.executeUpdate( "INSERT INTO files VALUES("+ path +");" ); }
		catch( SQLException e ) { e.printStackTrace(); return 0; }
	}

	public boolean removeFile( int fileId ) {
		try {
			return stmt.executeUpdate( "DELETE FROM files WHERE file_ID = "+ fileId +";" ) > 0
			? true : false;
		} catch( SQLException e ) { e.printStackTrace(); return false; }
	}
	
	public int update( int fileId, String value ) {
		try {
			return stmt.executeUpdate( "UPDATE files SET path = "+ value +" WHERE file_ID = "+ fileId +";" );
		} catch( SQLException e ) { e.printStackTrace(); return 0; }
	}
}
