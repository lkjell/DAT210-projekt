/**
	Document field
*/

package server;

import org.eclipse.jetty.server.Server;

public class TestServer extends Server {
	
	public TestServer() {
		super(8080);
//		ContextHandler context = new ContextHandler();
//        context.setContextPath("../web");
//        context.setResourceBase(".");
//        context.setClassLoader(Thread.currentThread().getContextClassLoader());
//        context.setHandler(new TestHandler());
//		this.setHandler(context);
		this.setHandler(new TestHandler());
		ServerTrayIcon.make(this);
	}
}
