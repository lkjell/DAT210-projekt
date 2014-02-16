/**
	Document field
*/

package server;

import org.eclipse.jetty.server.Server;

public class TestServer extends Server {
	
	public TestServer() {
		super(8080);
<<<<<<< HEAD
<<<<<<< HEAD
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[] { "/web/index.html" });
        resource_handler.setResourceBase(".");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        super.setHandler(handlers);
        try {
			super.start();
	        super.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
=======
//		ContextHandler context = new ContextHandler();
//        context.setContextPath("../web");
//        context.setResourceBase(".");
//        context.setClassLoader(Thread.currentThread().getContextClassLoader());
//        context.setHandler(new TestHandler());
//		this.setHandler(context);
		this.setHandler(new TestHandler());
		ServerTrayIcon.make(this);
>>>>>>> 3bfade2dae7bd2471c0631ffdc7bdbe1490f82d4
=======
		this.setHandler(new TestHandler());
>>>>>>> parent of 9f66c2a... bildene komme ihvertfall opp ;)
	}
}
