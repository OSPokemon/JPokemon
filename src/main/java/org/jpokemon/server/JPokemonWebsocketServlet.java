package org.jpokemon.server;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.zachtaylor.emissary.WebsocketConnection;

public class JPokemonWebsocketServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new WebsocketConnection();
	}
}
