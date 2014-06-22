package org.jpokemon.server;

import java.util.concurrent.ConcurrentHashMap;

import org.jpokemon.api.PokemonTrainer;
import org.json.JSONObject;
import org.zachtaylor.emissary.Emissary;
import org.zachtaylor.emissary.WebsocketConnection;
import org.zachtaylor.emissary.event.WebsocketConnectionClose;
import org.zachtaylor.emissary.event.WebsocketConnectionOpen;

public class PlayerRegistry extends Emissary {
	public static final PlayerRegistry instance = new PlayerRegistry();

	protected ConcurrentHashMap<String, WebsocketConnection> connections = new ConcurrentHashMap<String, WebsocketConnection>();

	private PlayerRegistry() {
		register(WebsocketConnectionOpen.class, this);
		register(WebsocketConnectionClose.class, this);
	}

	public WebsocketConnection getWebsocketConnection(String name) {
		return connections.get(name);
	}

	@Override
	public void serve(WebsocketConnection connection, JSONObject json) {
		String name = json.getString("username");
		String password = json.getString("password");

		PokemonTrainer pokemonTrainer = PokemonTrainer.manager.getByName(name);
		PasswordData passwordData = null;

		for (Object metaData : pokemonTrainer.getMetaData()) {
			if (metaData instanceof PasswordData) {
				passwordData = (PasswordData) metaData;
				break;
			}
		}

		if (passwordData == null || passwordData.getPassword().equals(password)) {
			connections.put(name, connection);
			connection.setEmissary(new DefaultEmissary());
			connection.send(new JSONObject("{\"event\":\"login\"}"));
		}
	}

	public void handle(WebsocketConnectionOpen event) {
		event.getWebsocket().setEmissary(this);
	}

	public void handle(WebsocketConnectionClose event) {
		String name = event.getWebsocket().getName();

		if (name != null) {
			connections.remove(name);
		}
	}

	public static void guaranteeClassLoad() {
	}
}
