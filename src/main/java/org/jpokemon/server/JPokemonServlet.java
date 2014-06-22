package org.jpokemon.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.zachtaylor.emissary.WebsocketConnection;

/**
 * Serves up files and lets you establish a websocket connection
 * 
 * @author zach
 * 
 */
public class JPokemonServlet extends WebSocketServlet {
	public static String webdir = "src/main/www";
	public static String cache = "no-store,no-cache,must-revalidate";

	protected ResourceHandler resourceHandler;

	public JPokemonServlet() {
		resourceHandler = new ResourceHandler();
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setCacheControl(cache);
		resourceHandler.setResourceBase(webdir);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resourceHandler.handle(req.getRequestURI(), (Request) req, req, resp);

		// if (!resp.isCommitted()) {
		// resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		// resp.setContentType("text/html");
		// resp.getWriter().println(
		// "<h1>Resource Not Found</h1><br/>Sorry, the resource you've requested could not be located.");
		// }
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new WebsocketConnection();
	}

	private static final long serialVersionUID = 1L;
}