/**
	Document field
*/

package package1;

import org.eclipse.jetty.server.Server;

public class TestServer {

	public static void main(String[] args) throws Exception{
		System.out.print("faggot");
		Server server = new Server(80);
		server.setHandler(new TestHandler());
		server.start();
		server.join();
	}

}
