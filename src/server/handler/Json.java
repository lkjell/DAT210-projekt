package server.handler;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import database.Query;

public class Json extends AbstractHandler {
	
	Query query;
	
	public Json( Query query ) { this.query = query; }

	@Override
	public void handle(String target, Request baseReq, HttpServletRequest req,
			HttpServletResponse rsp) throws IOException, ServletException {
		String method = req.getMethod();
		if( method.equals( "GET" )) {
			
			// http://shiflett.org/blog/2011/may/the-accept-header
			// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			// http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
			// https://restful-api-design.readthedocs.org/en/latest/resources.html
			String accept = req.getHeader( "Accept" );
			if( accept.contains( "application/x-resource+json" )
					|| accept.contains( "application/x-collection+json" )
					|| accept.contains( "application/json" )) {
				
				String img = req.getParameter( "img" );
				if( img != null ) {
					ResultSet kw = null;
					try { kw = query.getKeywords( Integer.parseInt( img )); }
					catch( NumberFormatException e ) { e.printStackTrace(); }
					StringBuilder kwlist = new StringBuilder();
					boolean first = true;
					try {
						while(kw.next()) {
							if( !first ) { kwlist.append( ",\"" ); }
							kwlist.append( kw.getString( 1 ) +'"' );
							first = false;
						}
					} catch (SQLException e) { e.printStackTrace(); }
					rsp.getWriter().println( "{\"XPKeywords\":["+ kwlist +"]}" );
				}
				
			} else {
				System.out.println( "Unknown Accept header in GET request: \""+ accept +"\"" );
			}
			
		} else if( method.equals( "POST" )) {
			
		} else if( method.equals( "PUT" )) {
			
		} else if( method.equals( "DELETE" )) {
			
		}
	}
}
