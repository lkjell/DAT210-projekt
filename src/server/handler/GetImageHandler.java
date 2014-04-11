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

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import database.Query;

/**
 * @author andreas
 *
 */
public class GetImageHandler extends AbstractHandler {

	//constructor
	public GetImageHandler() {
		super();
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
		int id = Thread.currentThread().hashCode();;
		// Hvis ikke foerste request, send til neste handler.
		if ( !baseRequest.getRequestURI().startsWith("/img/")) {
			System.out.println(id +" en fil med feil path " +baseRequest.getRequestURI());return; 
			}
		
		
		System.out.println(id + " Entrer " + this.getClass() + " :" + baseRequest.getRequestURI());
		
		
		
		//mappe funnet
		System.out.println(id +" img dir found");
		
		
		String imageExtension = null;
		int fileId = 0;
		byte[] b = null;

		//hente fil id fra requesten mottat
		try {
			fileId = Integer.parseInt(request.getParameter("img_id"));
			System.out.println(id +" parse filid success! result: " + fileId);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); System.out.println(id +" id er ikke int!?" + " file id:" + fileId + request.getRequestURL());
			return;
		} 
		
		//henter filen ved hjelp av databasen
		File image = new Query().getFile( fileId );
		System.out.println(id +" database lookup done");
		
		//bytearray for å holde bildet
		b = new byte[(int)image.length()];
		
		//henter imagepath og filextension
		String imagePath = image.getPath();
		imageExtension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
		
		
		FileInputStream input = new FileInputStream(image);
		int antallSkrevet = input.read(b);
		System.out.println(id +" skrev " + antallSkrevet + " bytes av: "+ imagePath);

		//lager header
		response.setContentType("image/"+ imageExtension);
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		
		//henter og skriver til responsen(tømmer buffer) og lukker streams
		ServletOutputStream os = response.getOutputStream();
		os.write(b);
		os.flush();
		input.close();
		System.out.println(id +" forlater " + this.getClass() + " :" + baseRequest.getRequestURI());
	}
}
