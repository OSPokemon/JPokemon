package org.jpokemon.server;

import java.util.concurrent.ConcurrentHashMap;

import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.server.event.PokemonTrainerLogin;
import org.json.JSONObject;
import org.zachtaylor.emissary.Emissary;
import org.zachtaylor.emissary.WebsocketConnection;
import org.zachtaylor.emissary.event.WebsocketConnectionClose;
import org.zachtaylor.emissary.event.WebsocketConnectionOpen;

import com.sun.jersey.core.util.Base64;

public class PlayerRegistry extends Emissary {
	private static final PlayerRegistry instance = new PlayerRegistry();

	protected ConcurrentHashMap<String, WebsocketConnection> connections = new ConcurrentHashMap<String, WebsocketConnection>();

	private PlayerRegistry() {
		register(WebsocketConnectionOpen.class, this);
		register(WebsocketConnectionClose.class, this);
	}

	public static WebsocketConnection getWebsocketConnection(String name) {
		return instance.connections.get(name);
	}

	@Override
	public void serve(WebsocketConnection connection, JSONObject json) {
		String name = json.getString("username");
		String password = json.getString("password");

		PokemonTrainer pokemonTrainer = PokemonTrainer.manager.getByName(name);

		if (pokemonTrainer == null) { // NOOP
			return;
		}

		UserIdentityProperty UserIdentityProperty = pokemonTrainer.getProperty(UserIdentityProperty.class);

		if (UserIdentityProperty == null || UserIdentityProperty.getPassword().equals(password)) {
			connections.put(name, connection);
			connection.setName(name);
			connection.setEmissary(new DefaultEmissary());

			JSONObject loginEventJson = new JSONObject();
			loginEventJson.put("event", "login");
			connection.send(loginEventJson);
		}

		if (UserIdentityProperty != null && UserIdentityProperty.getRoles().contains("admin")) {
			JSONObject adminEventJson = new JSONObject();
			adminEventJson.put("event", "admin");
			adminEventJson.put("authorizationHeader", "Basic " + new String(Base64.encode(name + ':' + password)));
			connection.send(adminEventJson);
		}

		post(new PokemonTrainerLogin(pokemonTrainer, connection));
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
