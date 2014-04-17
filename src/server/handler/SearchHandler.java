package server.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import database.Query;

public class SearchHandler extends ContextHandler {

	private static Logger log = LogManager.getLogger( SearchHandler.class.getName() );
	
	public SearchHandler() {
		this.setHandler( new innerHandler() );
	}

	public SearchHandler( Context context ) {
		super( context );
		this.setHandler( new innerHandler() );
	}

	public SearchHandler( String contextPath ) {
		super( contextPath );
		this.setHandler( new innerHandler() );
	}

	public SearchHandler( HandlerContainer parent, String contextPath ) {
		super( parent, contextPath );
		this.setHandler( new innerHandler() );
	}
	
	private String askDB( String searchstring ) {
		Query q = new Query();
		return q.search( searchstring ).toString();
	}
	
	private class innerHandler extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			log.debug( baseRequest.getUri() );
			
			if( !request.getMethod().equals( "GET" )) return;
			
			String jsonString = askDB( request.getParameter( "string" ) );
			log.debug( jsonString );
			response.setContentType( "application/json" );
			response.getWriter().println( jsonString );
			baseRequest.setHandled( true );
		}
	}
}
