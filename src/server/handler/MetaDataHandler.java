package server.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import database.Query;

/**
 * Handles GET requests with uri: /meta*
 * @author Joakim
 */
public class MetaDataHandler extends ContextHandler {

	private static Logger log = LogManager.getLogger( MetaDataHandler.class.getName() );
	
	public MetaDataHandler( String context ) {
		super();
		this.setContextPath( context );
		this.setHandler( new innerHandler() );
	}
	
	/**
	 * Does the outer class' job
	 */
	private class innerHandler extends AbstractHandler {

		// http://shiflett.org/blog/2011/may/the-accept-header
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
		// http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
		// https://restful-api-design.readthedocs.org/en/latest/resources.html
		
		/**
		 * Handles GET requests with uri: /meta*
		 */
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request,
				HttpServletResponse response ) throws IOException, ServletException {
			
			String method = request.getMethod();
			
			if( method.equals( "GET" )) {
				
				log.debug( baseRequest.getUri() );
				String accept = request.getHeader( "Accept" );
				if( accept.contains( "application/x-resource+json" )
						|| accept.contains( "application/x-collection+json" )
						|| accept.contains( "application/json" )) {
					log.debug( "Accept header: \""+ accept +"\"" );
					
					sendMetadata( request, response );
					baseRequest.setHandled( true );
					
				} else log.warn( "Unknown Accept header in GET request: \""+ accept +"\"" );
				
			}else if( method.equals( "POST" ))
				
				log.warn( "POST handler is not implemented "+ baseRequest.getRequestURI() );
		}
		
		/**
		 * Parses uri parameter and sends response
		 */
		private void sendMetadata( HttpServletRequest request, HttpServletResponse response ) throws IOException {
			
			String img_id = request.getParameter( "img_id" );
			String jsonString;
			if( img_id != null ) {
				int id = 0;
				try {
					id = Integer.parseInt( img_id );
					jsonString = getMetadata( id );
					response.setStatus( HttpServletResponse.SC_OK );
				} catch( NumberFormatException e ) {
					response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
					jsonString = "{\"error\":\"image id "+ img_id +" is not an integer.\"}";
					log.warn( jsonString );
				}
			} else {
				response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
				jsonString = "{\"error\":\"image id is null.\"}";
				log.warn( jsonString );
			}
			response.setContentType( "application/json" );
			response.getWriter().println( jsonString );
		}
		
		/**
		 * Retrieves from database and builds json string
		 */
		private String getMetadata( int id ) {
			
			Query q = new Query();
			String path = q.getPath( id ).replace( '\\', '/' );
			String[] kw = q.getKeywords( id );
			short[] dim = q.getDimensions( id );
			q = null;
			StringBuilder kwlist = new StringBuilder();
			for ( int i=0; i<kw.length; i++ )
				kwlist.append(( i==0 ? "" : "," ) +"\""+ kw[i] +"\"" );
			String jsonString = "{ \"path\": \""+ path +"\""
					+ ",\n  \"width\": "+ dim[0]
					+ ",\n  \"height\": "+ dim[1]
					+ ",\n  \"keywords\": ["+ kwlist +"]\n}";
			log.debug( "sending metadata\n"+ jsonString );
			return jsonString;
		}
	}
}
