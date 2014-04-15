/**
 * 
 */
package server.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import database.Query;

/**
 * @author andreas
 *
 */
public class GetImageHandler extends ContextHandler {

	private static Logger log = LogManager.getLogger( GetImageHandler.class.getName() );
	
	//constructor
	public GetImageHandler( String context ) {
		super();
		this.setContextPath( context );
		this.setHandler( new innerHandler() );
	}

	private class innerHandler extends AbstractHandler {
		
		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {

			log.debug( baseRequest.getUri() );

			String imageExtension = null;
			int fileId = 0;
			byte[] b = null;

			//hente fil id fra requesten mottat
			String img_id = request.getParameter( "img_id" );
			try {
				fileId = Integer.parseInt( img_id );
				log.debug( "parse fileId success! result: " + fileId );
			} catch (NumberFormatException e) {
				log.warn( "{\"error\":\"image id "+ img_id +" is not an integer.\"}" );
				return;
			}

			//henter filen ved hjelp av databasen
			File image = new Query().getFile( fileId );

			//bytearray for å holde bildet
			b = new byte[(int)image.length()];

			//henter imagepath og filextension
			String imagePath = image.getPath();
			imageExtension = imagePath.substring( imagePath.lastIndexOf( '.' ) + 1 );


			FileInputStream input = new FileInputStream(image);
			int antallSkrevet = input.read(b);

			//lager header
			response.setContentType("image/"+ imageExtension);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);

			//henter og skriver til responsen(tømmer buffer) og lukker streams
			ServletOutputStream os = response.getOutputStream();
			os.write(b);
			os.flush();
			input.close();
			log.debug( "skrev " + antallSkrevet + " bytes av: "+ imagePath );
		}
	}
}
