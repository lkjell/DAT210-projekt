package server;

public class Main {

	public static void main( String[] args ) throws Exception{

		Config cnfg = new Config( "config.ini" );
		MetaNetServer server = new MetaNetServer( cnfg );
		System.out.println( "Continued main thread. Exiting gracefully." );
		server.trayicon.remove();
		System.exit( 0 ); // exit gracefully
	}
}
