package server;

public class Main {

	public static void main(String[] args) throws Exception{
		//new HTMLpage();
		TestServer server = new TestServer();
<<<<<<< HEAD
//		int timeToWait = 10*1000; // x*1000 ms
//		server.wait(timeToWait);
//		System.out.println("Server up for " + timeToWait + ", stopping...");
//		server.stop();
//		System.out.println("Server has stopped.");
=======
		server.start();
		server.join();
		System.out.println( "Continued main thread. Exiting gracefully." );
		System.exit(0); // exit gracefully
>>>>>>> 3bfade2dae7bd2471c0631ffdc7bdbe1490f82d4
	}
}
