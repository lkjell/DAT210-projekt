/**
	Document field
*/

package server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;

 	public class TestServer extends Server {
 		
 		public TestServer(int portN) {
 			super(portN);
			this.setHandler(new TestHandler());
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
 		}
 	}
