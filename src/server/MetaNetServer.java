/**
	Document field
*/

package server;

import java.net.BindException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.jetty.server.Connector;
//import org.apache.commons.imaging.ImageReadException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import server.handler.*;
import database.CreateDB;
import database.Query;


public class MetaNetServer extends Server {
	
	public static final int DEFAULT_PORT = 8080;
	Config cnfg;
	ServerTrayIcon trayicon;
	final database.Query query = new Query();
	
 	public MetaNetServer( Config cnfg ) throws Exception {
 		this.cnfg = cnfg;
 		ServerConnector connector = new ServerConnector( this );
 		connector.setPort( cnfg.port );
 		this.setConnectors( new Connector[]{ connector });
 		
 		//handler requests for alle filene webIndex spesifiserer (ie stylesheet.css)
	    ResourceHandler webDirHandler = new ResourceHandler();
	    webDirHandler.setDirectoriesListed( true );
	    webDirHandler.setWelcomeFiles( new String[] { cnfg.webIndex });
	    webDirHandler.setResourceBase( cnfg.webDir );
	    
	    
	    GetImageHandler imageHandler = new GetImageHandler(query);
	    
	    //
	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers( new Handler[] {
	    		
	    		//leverer html
	    		new HandlerHTML( "web/index.html" , query),
	    		
	    		//leverer bilder
	    		imageHandler,	    		 
	    		
	    		//leverer web docs
	    		webDirHandler, 
	    		
	    		//que? no comprende last effort 404
	    		new DefaultHandler() });
	    ContextHandler omnipotentHandler = new ContextHandler();
	    omnipotentHandler.setContextPath( "/" );
	    omnipotentHandler.setHandler( handlers );
	    
	    
	    ResourceHandler rh1 = new ResourceHandler();
	    rh1.setResourceBase( "./img" ); //TODO: Replace with query to database to get specific image path
	    ContextHandler ch1 = new ContextHandler();
	    ch1.setContextPath( "/img" );
	    ch1.setHandler( rh1 );
	    
	    ContextHandler ch2 = new ContextHandler();
	    ch2.setContextPath( "/getTags" );
	  //ch2.setHandler( new HandlerMetadataRequest() );
	    ch2.setHandler( new Json(query));
	    
	    ContextHandlerCollection contexts = new ContextHandlerCollection();
	    contexts.setHandlers( new Handler[]{ omnipotentHandler, ch1, ch2 });
	    this.setHandler( contexts );
	    
		trayicon = new ServerTrayIcon( this );
		
		CreateDB.main( new String[]{""} );
		query.addFiles("C:/Users/andreas/Dropbox/Bilder");
		//query.removeFile(1);
		query.notMain( );
		
	    try {
			this.start(); // Attempt to bind server to given port
		    this.join();  // Pause this thread while server runs
		}
		catch ( BindException e ) {
			JOptionPane.showMessageDialog( new JFrame(),
				"A process is already listening on port "+ cnfg.port );
		}
 	}
 }
