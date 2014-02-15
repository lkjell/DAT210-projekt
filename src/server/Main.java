package server;

public class Main {

	public static void main(String[] args) throws Exception{
		//new HTMLpage();
		TestServer server = new TestServer();
		server.start();
		server.join();
		System.out.println( "Continued main thread. Exiting gracefully." );
		System.exit(0); // exit gracefully
	}
}
