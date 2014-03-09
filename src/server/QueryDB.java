package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryDB {
	
	public static final String SQL_STATEMENT1 = "select * from files";
	public static final String SQL_STATEMENT2 = "select * from relations";
	public static final String SQL_STATEMENT3 = "select * from tags";
	
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
		for (int x = 1; x <= columnCount; x++)
			System.out.format("%20s", table.getMetaData().getColumnName(x) + " | ");
		while (table.next()){
			System.out.println("");
			for (int x = 1; x <= columnCount; x++)
				System.out.format("%20s", table.getString(x) + " | ");
		}
		System.out.println("");
		System.out.println("");
	}
}
