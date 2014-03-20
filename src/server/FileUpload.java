package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class FileUpload extends AbstractHandler{

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) 
			throws IOException, ServletException{
	
		if (target.equals("/fileupload")){
			response.setContentType("text/html;charset=utf-8");
			baseRequest.setHandled(true);
			try{
		
			PrintWriter outp = response.getWriter();
			StringBuffer buff = new StringBuffer();
		
			File file1 = (File) request.getAttribute( "userfile1" );
		
			if( file1 == null || !file1.exists() ){
				buff.append( "File does not exist" );
			
			}else if( file1.isDirectory()){
				buff.append( "File is a directory" );
			
			}else{
				File outputFile = new File( request.getParameter( "userfile1" ) );
				file1.renameTo( outputFile );
				buff.append( "File successfully uploaded." );
			}
		
			outp.write( "" );
			outp.write( "FileUpload page" );
			outp.write( "" );
			outp.write( "" + buff.toString() + "" );
			outp.write( "" );
			outp.write( "" );
			
		
			}catch(Exception ee){
				System.out.println(ee.getMessage());
			}
		}
	}
}
