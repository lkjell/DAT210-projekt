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
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class FileUploadHandler extends AbstractHandler {

	private String savePath = "c:\\";
	private String tempFolder = "c:\\temp";
	private File file ;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,HttpServletResponse response) 
			throws IOException, ServletException {
		int id = Thread.currentThread().hashCode();

		if(!request.getMethod().equals("POST")) return;

		System.out.println(id +" Entrer " + this.getClass() + " :" + baseRequest.getRequestURI());
		System.out.println(id +" Request is a POST request");

		if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
			System.out.println(id +" ContentType is MultiPart");

			// Check that we have a file upload request
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if( isMultipart) System.out.println(id +" File is multipart");

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
					}
				}
				out.println("</body>");
				out.println("</html>");
			}catch(Exception ex) {
				System.out.println(ex);
			}
		}

		System.out.println(request.toString());
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		System.out.println(id +" forlater " + this.getClass() + " :" + baseRequest.getRequestURI());

	}
}
