/**
	Document field
*/

package server;

import org.eclipse.jetty.server.Server;

public class TestServer extends Server {
	
	private int port;
	
	public TestServer() { this( 80 ); }
	public TestServer( int port ) {
		super( port );
//		ContextHandler context = new ContextHandler();
//        context.setContextPath("../web");
//        context.setResourceBase(".");
//        context.setClassLoader(Thread.currentThread().getContextClassLoader());
//        context.setHandler(new TestHandler());
//		this.setHandler(context);
		this.setHandler( new TestHandler() );
		ServerTrayIcon.make( this );
		this.port = port;
	}
	
	public void test() {}
	
	public int getPort() { return port;	}
}
