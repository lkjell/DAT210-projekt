package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateDB {
	
	public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String JDBC_URL = "jdbc:derby:MetaDB;create=true";
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Class.forName(DRIVER);
		Connection connection = DriverManager.getConnection(JDBC_URL);
		connection.createStatement().execute("create table files("
											+ "channel varchar(20), "
											+ "topic varchar(20), "
											+ "videoclip varchar(20))");
		connection.createStatement().execute("insert into files values"
											+ "('oodp', 'creational', 'singleton'),"
											+ "('oodp', 'creational', 'factory method'),"
											+ "('oodp', 'creational', 'abstract factory')");
		
		connection.createStatement().execute("create table relations("
											+ "channel varchar(20), "
											+ "topic varchar(20), "
											+ "videoclip varchar(20))");
		connection.createStatement().execute("insert into relations values"
											+ "('oodp', 'creational', 'singleton'),"
											+ "('oodp', 'creational', 'factory method'),"
											+ "('oodp', 'creational', 'abstract factory')");
		
		connection.createStatement().execute("create table tags("
											+ "channel varchar(20), "
											+ "topic varchar(20), "
											+ "videoclip varchar(20))");
		connection.createStatement().execute("insert into tags values"
											+ "('oodp', 'creational', 'singleton'),"
											+ "('oodp', 'creational', 'factory method'),"
											+ "('oodp', 'creational', 'abstract factory')");
		
		System.out.println("files table created and records successfully inserted");
		System.out.println("relations table created and records successfully inserted");
		System.out.println("tags table created and records successfully inserted");
	}
	
	

}
