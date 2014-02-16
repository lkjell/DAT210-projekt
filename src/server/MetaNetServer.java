/**
	Document field
*/

package server;

import java.net.BindException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class MetaNetServer extends Server {
	
	int port;
	
 	public MetaNetServer( Config cnfg ) throws Exception {
 		
 		super( cnfg.getPort() );
 		port = cnfg.getPort();
	    ResourceHandler resource_handler = new ResourceHandler();
	    resource_handler.setDirectoriesListed( true );
	    resource_handler.setWelcomeFiles( new String[] { cnfg.getWebIndex() });
	    resource_handler.setResourceBase( "." );
	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers( new Handler[] { new RequestFilename(), resource_handler, new DefaultHandler() });
	    this.setHandler( handlers );
		ServerTrayIcon.make( this );
	    try {
			this.start(); // Attempt to bind server to given port
		    this.join();  // Pause this thread while server runs
		}
		catch ( BindException e ) {
			JOptionPane.showMessageDialog( new JFrame(),
				"A process is already listening on port "+ port );
		}
 	}
 }
