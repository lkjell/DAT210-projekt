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
											+ "File_ID int, "
											+ "File_Title varchar(64),"
											+ "primary key(File_ID)"
											+ ")");
		connection.createStatement().execute("insert into files values"
											+ "(1, 'something.jpg'),"
											+ "(2, 'IMG_124.jpg')");
		
		connection.createStatement().execute("create table tags("
											+ "Tag_ID int, "
											+ "Tag_Title varchar(64),"
											+ "primary key(Tag_ID)"
											+ ")");
		connection.createStatement().execute("insert into tags values"
											+ "(1, 'kitten'),"
											+ "(2, 'cat')");
		
		connection.createStatement().execute("create table relations("
											+ "File_ID int, "
											+ "Tag_ID int,"
											+ "foreign key(File_ID) references files(File_ID),"
											+ "foreign key(Tag_ID) references tags(Tag_ID)"
											+ ")");
		
		connection.createStatement().execute("insert into relations values"
											+ "(2, 1),"
											+ "(2, 1)");
		
		System.out.println("files table created and records successfully inserted");
		System.out.println("relations table created and records successfully inserted");
		System.out.println("tags table created and records successfully inserted");
	}
	
	

}
