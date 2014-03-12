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
											+ "file_ID int, "
											+ "path varchar(256),"
											+ "primary key(file_ID)"
											+ ")");
		connection.createStatement().execute("insert into files values"
											+ "(1, 'test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/something.jpg'),"
											+ "(2, 'IMG_124.jpg')");
		
		connection.createStatement().execute("create table tags("
											+ "tag_ID int, "
											+ "tag_Title varchar(64),"
											+ "primary key(tag_ID)"
											+ ")");
		connection.createStatement().execute("insert into tags values"
											+ "(1, 'kitten'),"
											+ "(2, 'cat')");
		
		connection.createStatement().execute("create table relations("
											+ "file_ID int, "
											+ "tag_ID int,"
											+ "foreign key(file_ID) references files(file_ID),"
											+ "foreign key(tag_ID) references tags(tag_ID)"
											+ ")");
		
		connection.createStatement().execute("create table dirs("
											+ "dir_ID int, "
											+ "path varchar(256), "
											+ "primary key(dir_ID)"
											+ ")");
		
		connection.createStatement().execute("insert into relations values"
											+ "(2, 1),"
											+ "(2, 1)");
		
		System.out.println("files table created and records successfully inserted");
		System.out.println("relations table created and records successfully inserted");
		System.out.println("tags table created and records successfully inserted");
		System.out.println("dirs table created and records successfully inserted");
	}
	
	

}
