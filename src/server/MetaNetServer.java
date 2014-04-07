/**
	Document field
*/

package server;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
	    handlers.setHandlers( new Handler[] { new FileUploadHandler(), new HTMLhandler( cnfg.getWebIndex()), 
	    		/*new RequestFilename(),*/ resource_handler, new DefaultHandler() });
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
 	
class HTMLhandler extends AbstractHandler {
 		
 		String path;
 		
 		public HTMLhandler( String path ) { this.path = path; }
 		
 		private String editHTML() throws IOException {
 			
 			File dir = new File("img/");
 			//String[] list_of_files = dir.list();
 			List<File> list_of_files = new LinkedList<File>();
 			
 			/* Should use recursive? */
 			for(File file:dir.listFiles()) {
 				if(file.isFile()) {
 					list_of_files.add(file);
 				} else if(file.isDirectory()) {
 					for (File subfile:file.listFiles()) {
 						if(subfile.isFile())
 							list_of_files.add(subfile);
 					}
 				}
 			}
 			
 	 		Document doc = Jsoup.parse( new File( path ), "utf-8" );
 	 		Element images = doc.getElementsByClass( "images" ).first();
 	 		
 	 		for ( File filename: list_of_files ) {
 	 			images.appendElement( "li" ).attr( "class", "image" ).attr( "id", "someNumber" )
 	 					.appendElement( "img" ).attr( "src", filename.getPath() ).attr( "alt", "Request did not succeed" );
 	 		}
 	 		
 	 		//System.out.println(images);
 	 		return doc.toString();
 	 	}
 		
		@Override
		public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
		        throws IOException, ServletException {
			
			// Hvis ikke f�rste request, send til neste handler.
			if ( !baseRequest.getRequestURI().equals( "/" )) {
				 baseRequest.setHandled(false);
				 return;
			}
//			Enumeration<String> headers = baseRequest.getHeaderNames();
//			System.out.println( "Headers:" );
//			for ( String s: Collections.list(headers) ) {
//				String v = baseRequest.getHeader( s );
//				System.out.println(s+" : "+v);
//			}
//			System.out.println(baseRequest.getRequestURI());

		    response.setContentType("text/html;charset=utf-8");
		    response.setStatus(HttpServletResponse.SC_OK);
		    baseRequest.setHandled(true);
		    response.getWriter().println( editHTML() );
		}
 	}
 }
