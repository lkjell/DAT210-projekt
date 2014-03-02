package server;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class HandlerHTML extends AbstractHandler {
	
	String path;
	
	public HandlerHTML( String path ) { this.path = path; }
	
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
		
		// Leser foerste filen i rekka og printer ut metadata
		/*try {
			MetadataExample.read( list_of_files.get(0) );
		} catch (ImageReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
 		Document doc = Jsoup.parse( new File( path ), "utf-8" );
 		Element images = doc.getElementsByClass( "images" ).first();
 		
 		for ( File file: list_of_files ) {
 			images.appendElement( "li" ).attr( "class", "image" ).attr( "id", "someNumber" )
 					.appendElement( "img" ).attr( "src", file.getPath().replace("\\", "/")).attr( "alt", "Request did not succeed" );
 		}
 		return doc.toString();
 	}
	
	@Override
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException {

		// Hvis ikke foerste request, send til neste handler.
		if ( !baseRequest.getRequestURI().equals( "/" )) { return; }

	    response.setContentType("text/html;charset=utf-8");
	    response.setStatus(HttpServletResponse.SC_OK);
	    baseRequest.setHandled(true);
	    response.getWriter().println( editHTML() );
	}
}