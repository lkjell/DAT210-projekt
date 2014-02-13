package server;

public class Main {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//new HTMLpage();
		TestServer server = new TestServer();
		server.start();
		server.join();
	}
}
