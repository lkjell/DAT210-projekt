/**
	Document field
*/

package server;

import org.eclipse.jetty.server.Server;

public class TestServer extends Server {
	
	public TestServer() {
		super(8080);
		this.setHandler(new TestHandler());
	}
}
