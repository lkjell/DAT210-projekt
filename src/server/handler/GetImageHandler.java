/**
 * 
 */
package server.handler;

import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * @author andreas
 *
 */
public class GetImageHandler extends ContextHandler {

	/**
	 * 
	 */
	public GetImageHandler() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public GetImageHandler(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param contextPath
	 */
	public GetImageHandler(String contextPath) {
		super(contextPath);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 * @param contextPath
	 */
	public GetImageHandler(HandlerContainer parent, String contextPath) {
		super(parent, contextPath);
		// TODO Auto-generated constructor stub
	}

}
