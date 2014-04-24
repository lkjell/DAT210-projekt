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
	

	String imageDirectory = "C:/Users/andreas/Dropbox/Bilder/";
	//String imageDirectory = "C:/Users/Public/Pictures";

	
 	public MetaNetServer( Config cnfg ) throws Exception {
 		this.cnfg = cnfg;
 		ServerConnector connector = new ServerConnector( this );
 		connector.setPort( cnfg.port );
 		this.setConnectors( new Connector[]{ connector });
 		
 		//handler requests for alle filene webIndex spesifiserer (ie stylesheet.css)
	    ResourceHandler webDirHandler = new ResourceHandler();
	    webDirHandler.setResourceBase( cnfg.webDir );
	    
	    //
	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers( new Handler[] {
	    		
	    		
	    		//leverer html
	    		//new HtmlHandler( "web/index.html" ),

	    		//takler upload
	    		new FileUploadHandler(),
	    		
	    		//leverer web docs
	    		webDirHandler, 
	    		
	    		//que? no comprende last effort 404
	    		new DefaultHandler() });
	    ContextHandler omnipotentHandler = new ContextHandler();
	    omnipotentHandler.setContextPath( "/" );
	    omnipotentHandler.setHandler( handlers );
	    
	    ContextHandlerCollection contexts = new ContextHandlerCollection();
	    contexts.setHandlers( new Handler[]{
	    		omnipotentHandler,
	    		new MetaDataHandler( "/meta" ),	// Metadata Requests and Updates
	    		new GetImageHandler( "/img" ),	// File Download
	    		new SearchHandler( "/search" )	// Search results
	    });
	    this.setHandler( contexts );
	    
		trayicon = new ServerTrayIcon( this );
		
		CreateDB.main( new String[]{""} );
		Query query = new Query();
		query.addFilesRegex(".jpg$|.png$|.gif$", imageDirectory);
		//query.removeFile(1);
		query.printDatabase();
		query = null;
		
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
