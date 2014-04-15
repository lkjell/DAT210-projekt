package server.handler;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * @author Asbjorn
 */
public class FileUploadHandler extends AbstractHandler {

	private static Logger log = LogManager.getLogger( FileUploadHandler.class.getName() );
	private String savePath = "C:/Users/Public/Pictures/";
	private String tempFolder = "C:/Users/Public/Pictures/";
	private File file ;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,HttpServletResponse response) 
			throws IOException, ServletException {

		if( !request.getMethod().equals( "POST" )) return;

		log.debug( request.toString() );

		if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
			log.debug( "ContentType is MultiPart" );

			// Check that we have a file upload request
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if( isMultipart) log.debug( "File is multipart" );

			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();

			// Configure a repository (to ensure a secure temp location is used)
			factory.setRepository(new File(tempFolder));

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			try{ 
				// Parse the request to get file items.
				List<FileItem> fileItems = upload.parseRequest(request);

				// Process the uploaded file items
				Iterator<FileItem> i = fileItems.iterator();

				java.io.PrintWriter out = response.getWriter( );
				
				out.println("<html>");
				out.println("<head>");
				out.println("<title>Servlet upload</title>");  
				out.println("</head>");
				out.println("<body>");

				while ( i.hasNext () ){
					FileItem fi = (FileItem)i.next();
					if ( !fi.isFormField () ){
						// Get the uploaded file parameters
						//String fieldName = fi.getFieldName();
						String fileName = fi.getName();
						//String contentType = fi.getContentType();
						//boolean isInMemory = fi.isInMemory();
						//long sizeInBytes = fi.getSize();
						// Write the file
						if( fileName.lastIndexOf("\\") >= 0 ){
							file = new File( savePath + 
									fileName.substring( fileName.lastIndexOf("\\"))) ;
						}else{
							file = new File( savePath + 
									fileName.substring(fileName.lastIndexOf("\\")+1)) ;
						}
						fi.write( file ) ;
						out.println("Uploaded Filename: " + fileName + "<br>");
						log.debug( "Received a "+ fi.getSize() +" bytes file: "+ fileName );
					}
				}
				out.println("</body>");
				out.println("</html>");
			}catch(Exception ex) {
				System.out.println(ex);
			}
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}
}
