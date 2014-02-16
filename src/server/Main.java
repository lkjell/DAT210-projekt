package server;

import java.net.BindException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {

	public static void main( String[] args ) throws Exception{
		
		Config cnfg = new Config( "config.ini" );
		try {
			TestServer server = new TestServer( cnfg.getInt( "lipo" ));
			server.start();
			server.join();
		}
		catch ( BindException e ) {
			JOptionPane.showMessageDialog( new JFrame(),
				"A process is already listening on port "+ cnfg.getInt( "lipo" ));
		}
		System.out.println( "Continued main thread. Exiting gracefully." );
		System.exit( 0 ); // exit gracefully
	}
	
	
}
