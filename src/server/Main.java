package server;

public class Main {

	public static void main(String[] args) throws Exception{
		//new HTMLpage();
		TestServer server = new TestServer();
		server.start();
		server.join();
		/*
		 * burde kanskje legge inn vent 10 min så:
		 * server.stop();
		 */
	}
}
