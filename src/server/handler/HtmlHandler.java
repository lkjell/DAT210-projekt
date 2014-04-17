package server.handler;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import database.Query;

public class HtmlHandler extends AbstractHandler {
	//TODO: cookies 

	private static Logger log = LogManager.getLogger( HtmlHandler.class.getName() );
	
	String htmlPath;
	static int antallAksepterteHTMLKall = 0;
	
	public HtmlHandler( String path ) { this.htmlPath = path; }
	
	//metode for å klargjøre html til client
	private String editHTML() throws IOException {
		Query q = new Query();
		Integer[] files_id = q.getAllFileIds();

 		Document doc = Jsoup.parse( new File( htmlPath ), "utf-8" );
 		Element images = doc.getElementsByClass( "images" ).first();
 		
 		for (int i:files_id) {
			images.appendElement( "li" ).attr( "class", "image" ).attr( "id", Integer.toString(i) )
					.appendElement( "img" ).attr( "src", ("img/?img_id=" + i)).attr( "alt", "Request did not succeed" );
		}
 		return doc.toString();
	}
	
	/*
	File dir = new File("img/");
	//String[] list_of_files = dir.list();
	List<File> list_of_files = new LinkedList<File>();
	
	// Should use recursive?
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
	try {
		MetadataExample.read( list_of_files.get(0) );
	} catch (ImageReadException e) {
		// TODO Auto-generated catch block
		e.printStackTrace(System.out);
	}*/
	
	@Override
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException {

		String uri = baseRequest.getUri().toString();
		
		// Hvis ikke foerste request, send til neste handler.
		String contentType = request.getContentType();
		if ( !uri.equals( "/" ) || contentType != null && !contentType.contains( "text/html" )) { 
			log.debug( "Uri ikke acceptert: " + uri );
			return; }
		log.debug( baseRequest.getUri() );
		
		antallAksepterteHTMLKall++;
		
		log.debug("antallAksepterteHTMLKall: " + antallAksepterteHTMLKall);
	    response.setContentType("text/html;charset=utf-8");
	    response.setStatus(HttpServletResponse.SC_OK);
	    baseRequest.setHandled(true);
	    response.getWriter().println( editHTML() );
	    log.debug("Forlater " + this.getClass() + " :" + baseRequest.getRequestURI());
	}
}