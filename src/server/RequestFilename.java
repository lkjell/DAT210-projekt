package server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class RequestFilename extends AbstractHandler
{
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {

		String parameter = request.getParameter("filename");

		File dir = new File("img/");
		String[] list_of_files = dir.list();

		if ("all".equals(parameter)) {
			baseRequest.setHandled(true);
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);

			response.getWriter().println("Send all filename<br>");

			for(String index:list_of_files) {
				response.getWriter().println(index + "<br>");
			}
		} else {

			try {
				int index = Integer.parseInt(parameter);

				baseRequest.setHandled(true);
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);

				response.getWriter().println("Send file: " + index);

			} catch(Exception e){
				e.printStackTrace();
				baseRequest.setHandled(true);
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);

				response.getWriter().println("Invalid file index");
			}
		}
	}
}
