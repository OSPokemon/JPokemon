package org.jpokemon.server;

import org.jpokemon.api.Overworld;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.property.overworld.TmxFileProperties;
import org.jpokemon.property.trainer.AvatarsProperty;
import org.jpokemon.property.trainer.OverworldLocationProperty;
import org.jpokemon.server.event.PokemonTrainerLogin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zachtaylor.emissary.Emissary;
import org.zachtaylor.emissary.WebsocketConnection;

public class OverworldEmissary extends Emissary {
	public static final OverworldEmissary instance = new OverworldEmissary();

	private OverworldEmissary() {
		register(PokemonTrainerLogin.class, this);
	}

	@Override
	public void serve(WebsocketConnection arg0, JSONObject arg1) {

	}

	public void handle(PokemonTrainerLogin event) {
		PokemonTrainer pokemonTrainer = event.getPokemonTrainer();
		OverworldLocationProperty locationProperty = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		AvatarsProperty avatarsProperty = pokemonTrainer.getProperty(AvatarsProperty.class);

		if (locationProperty == null) {
			locationProperty = new OverworldLocationProperty();

			// TODO - make this customizable
			locationProperty.setOverworld("bedroom");
			locationProperty.setX(3);
			locationProperty.setY(3);

			pokemonTrainer.addProperty(locationProperty);
		}
		if (avatarsProperty == null) {
			avatarsProperty = new AvatarsProperty();

			// TODO - make this customizable
			avatarsProperty.addAvailableAvatar("default");
			avatarsProperty.setAvatar("default");

			pokemonTrainer.addProperty(avatarsProperty);
		}

		Overworld overworld = Overworld.manager.getByName(locationProperty.getOverworld());

		JSONObject mapJson = new JSONObject();
		mapJson.put("event", "overworld-load-map");
		mapJson.put("mapName", locationProperty.getOverworld());
		mapJson.put("tilesets", new JSONArray(overworld.getProperty(TmxFileProperties.class).getTileSets().toString()));

		JSONObject playerJson = new JSONObject();
		playerJson.put("name", pokemonTrainer.getName());
		playerJson.put("avatar", avatarsProperty.getAvatar());
		playerJson.put("x", locationProperty.getX());
		playerJson.put("y", locationProperty.getY());

		JSONArray playersArray = new JSONArray();
		playersArray.put(playerJson);

		synchronized (overworld) {
			for (String otherPlayerName : overworld.getPokemonTrainers()) {
				PokemonTrainer otherPokemonTrainer = PokemonTrainer.manager.getByName(otherPlayerName);
				JSONObject otherPlayerJson = new JSONObject();
				locationProperty = otherPokemonTrainer.getProperty(OverworldLocationProperty.class);
				avatarsProperty = otherPokemonTrainer.getProperty(AvatarsProperty.class);
				otherPlayerJson.put("name", otherPlayerName);
				otherPlayerJson.put("avatar", avatarsProperty.getAvatar());
				otherPlayerJson.put("x", locationProperty.getX());
				otherPlayerJson.put("y", locationProperty.getY());

				WebsocketConnection otherPlayerConnection = PlayerRegistry.getWebsocketConnection(otherPlayerName);
				otherPlayerConnection.send(playerJson);
				playersArray.put(otherPlayerJson);
			}

			mapJson.put("players", playersArray);
			event.getConnection().send(mapJson);

			overworld.addPokemonTrainer(pokemonTrainer.getName());
		}
	}
}
