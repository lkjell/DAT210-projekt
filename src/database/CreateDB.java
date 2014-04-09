package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateDB {

	public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String JDBC_URL = "jdbc:derby:MetaDB;create=true";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Class.forName(DRIVER);
		Connection connection = DriverManager.getConnection(JDBC_URL);
		connection.setAutoCommit(false);
		//lager tabell files
		try {
			connection.createStatement().execute("create table files("
					+ "file_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) , "
					+ "path varchar(256) constraint unique_path unique,"
					+ "primary key(file_id)"
					+ ")");
		} catch (Exception e) {
			String msg = e.getMessage();
			if( msg.startsWith( "Table/View 'FILES'" )) {
				System.out.println( msg );
			} else e.printStackTrace();
		}
		
		//putter eksempeldata inn i files
//		connection.createStatement().execute("insert into files (path) values"
//				+ "( 'test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/test/something.jpg'),"
//				+ "( 'IMG_124.jpg')");
//		System.out.println("files table created and records successfully inserted");

		//lager tabell xp_tag
		try {
			connection.createStatement().execute( "CREATE TABLE xp_tag("
					+ "xp_tag_ID INT NOT NULL GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), PRIMARY KEY(xp_tag_ID),"
					+ "tag VARCHAR(255) constraint tag_not_null NOT NULL constraint tag_unique UNIQUE" + ")" );
			System.out.println( "xp_tag table created" );
		} catch (Exception e) {
			String msg = e.getMessage();
			if( msg.startsWith( "Table/View 'XP_TAG'" )) {
				System.out.println( msg );
			} else e.printStackTrace();
		}

		//ny relations tabell
		try {
			connection.createStatement().execute( "CREATE TABLE relation("
					+ "file_ID INT, FOREIGN KEY (file_ID) REFERENCES files(file_ID) ON DELETE CASCADE,"
					+ "xp_tag_ID INT, FOREIGN KEY (xp_tag_ID) REFERENCES xp_tag(xp_tag_ID) ON DELETE CASCADE"+ ")" );
			System.out.println( "relation table created" );
		} catch (Exception e) {
			String msg = e.getMessage();
			if( msg.startsWith( "Table/View 'RELATION'" )) {
				System.out.println( msg );
			} else e.printStackTrace();
		}

		/*TRENGER VI DISSE?		
				connection.createStatement().execute("create table tags("
													+ "tag_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
													+ "tag_Title varchar(64),"
													+ "primary key(tag_ID)"
													+ ")");
				connection.createStatement().execute("insert into tags (tag_title) values"
													+ "( 'kitten'),"
													+ "( 'cat')");
				System.out.println("tags table created and records successfully inserted");

				connection.createStatement().execute("create table relations("
													+ "file_ID int, "
													+ "tag_ID int,"
													+ "foreign key(file_ID) references files(file_ID) on delete cascade,"
													+ "foreign key(tag_ID) references tags(tag_ID) on delete cascade"
													+ ")");

		connection.createStatement().execute("create table dirs("
				+ "dir_ID int, "
				+ "path varchar(256), "
				+ "primary key(dir_ID)"
				+ ")");
		System.out.println("dirs table created");
		
		connection.createStatement().execute("insert into relations values"
				+ "(2, 1),"
				+ "(2, 1)");
		System.out.println("relations table created and records successfully inserted");

*/

		connection.commit();
		connection.close();
	}
}
