package server.handler;

import java.io.IOException;
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
					|| accept.contains( "application/json" )
					|| accept.contains( "application/xml" )) {
				
				String img_id = req.getParameter( "img_id" );
				if( img_id != null ) {
					String[] kw = null;
					try { System.out.println("img_id i json :" + img_id); 
						kw = query.getKeywords( Integer.parseInt( img_id )); }
					catch( NumberFormatException e ) { e.printStackTrace(System.out); }
					StringBuilder kwlist = new StringBuilder();
					for (int i=0;i<kw.length;i++) {
						kwlist.append((i==0?"": ",")+ "\"" + kw[i] + "\"" );
					}
					String jsonString = "{\"XPKeywords\":["+ kwlist +"]}";
					System.out.println(jsonString);
					rsp.getWriter().println( jsonString );
					baseReq.setContentType("application/json");
					baseReq.setHandled( true );
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
