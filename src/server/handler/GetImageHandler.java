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

	Query query;

	public GetImageHandler(Query query) {
		super();
		this.query = query;
	}




	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
		// Hvis ikke foerste request, send til neste handler.
		if ( !baseRequest.getRequestURI().startsWith("/img/")) {
			System.out.println("en fil med feil path" +baseRequest.getRequestURI());return; 
			}
		System.out.println("img dir found");
		String imageExtension = null;
		int fileId = 0;
		byte[] b = null;

		try {
			fileId = Integer.parseInt(request.getParameter("img_id"));
			System.out.println("parse filid success! result: " + fileId);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); System.err.println("id er ikke int!?" + " file id:" + fileId + request.getRequestURL());

			return;
		} 
		File image = query.getFile(fileId);
		System.out.println("database lookup done");
		b = new byte[(int)image.length()];
		String imagePath = image.getPath();
		imageExtension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
		FileInputStream input = new FileInputStream(image);
		int antallSkrevet = input.read(b);
		System.out.println("skrev " + antallSkrevet + " bytes av: "+ imagePath);



		response.setContentType("image/"+ imageExtension);
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		ServletOutputStream os = response.getOutputStream();
		os.write(b);
		os.flush();
		input.close();

	}

}
