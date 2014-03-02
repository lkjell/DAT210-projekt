package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

class HandlerMetadataRequest extends AbstractHandler {
	
	public HandlerMetadataRequest() {}
	
	@Override
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException {

		//TODO: Replace with query to database for filepath
		String path = "";
		try { path = getClass().getClassLoader().getResource( "." ).toURI().resolve( ".." ).getPath(); }
		catch ( URISyntaxException e1 ) {}
		
		// Generate JSON string. Should use a JSON factory class
		File file = new File( path + target);
		String json = "{'filename':'"+ file.getName() +"'";
		BufferedImage bi = ImageIO.read( file );
		json += ",'width':'"+ bi.getWidth() +"'";
		json += ",'height':'"+ bi.getHeight() +"'";
		try {
			IImageMetadata metadata = Imaging.getMetadata( file );
			if ( metadata instanceof JpegImageMetadata ) {
	            final JpegImageMetadata jpegMetadata = ( JpegImageMetadata ) metadata;
				String kw = jpegMetadata.getExif().getFieldValue( MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS );
				json += ", 'exif':{'keywords':['"+ kw.replace( ";","','" ) +"']}";
			}
		} catch ( ImageReadException | NullPointerException e ) {}
		json += "}";
		// Now use proper quotes and remove control characters
		json = json.replace( "'", "\"" ).replaceAll( "\\p{Cc}","" );
		System.out.println( json );
				
	    response.setContentType( "application/json;charset=utf-8" );
	    response.setStatus( HttpServletResponse.SC_OK );
	    baseRequest.setHandled( true );
	    response.getWriter().println( json );
	}
}