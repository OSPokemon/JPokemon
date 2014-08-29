package org.jpokemon.action;

import java.util.List;

import org.jpokemon.api.Action;
import org.jpokemon.api.ActionFactory;
import org.jpokemon.api.ActionSet;
import org.jpokemon.api.JPokemonException;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.OverworldEntity;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.property.overworld.TmxFileProperties;
import org.jpokemon.property.trainer.AvatarsProperty;
import org.jpokemon.property.trainer.OverworldLocationProperty;
import org.jpokemon.server.PlayerRegistry;
import org.jpokemon.util.Options;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zachtaylor.emissary.WebsocketConnection;

public class OverworldTeleportAction implements Action {
	protected String overworld;

	protected int x;

	protected int y;

	public String getOverworld() {
		return overworld;
	}

	public void setOverworld(String overworld) {
		this.overworld = overworld;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void execute(Overworld playerLocationOverworld, OverworldEntity entity, ActionSet actionSet,
			PokemonTrainer pokemonTrainer) {
		OverworldLocationProperty teleportLocation = buildLocationProperty();

		if (playerLocationOverworld != null) {
			// send remove player json
			JSONObject json = new JSONObject();
			json.put("event", "overworld-remove-player");
			json.put("name", pokemonTrainer.getName());

			synchronized (playerLocationOverworld) {
				for (String playerInPlayerLocationOverworld : playerLocationOverworld.getPokemonTrainers()) {
					WebsocketConnection connection = PlayerRegistry.getWebsocketConnection(playerInPlayerLocationOverworld);
					connection.send(json);
				}

				playerLocationOverworld.removePokemonTrainer(pokemonTrainer.getName());
			}
		}

		// leh teleport!
		pokemonTrainer.addProperty(teleportLocation);

		Overworld destinationOverworld = Overworld.manager.getByName(teleportLocation.getOverworld());
		TmxFileProperties graphicsProperties = destinationOverworld.getProperty(TmxFileProperties.class);

		if (playerLocationOverworld == null || !playerLocationOverworld.getName().equals(teleportLocation.getOverworld())) {
			// send map load data to the new player

			JSONObject json = new JSONObject();
			json.put("event", "overworld-load-map");
			json.put("mapName", destinationOverworld.getName());
			json.put("tilesets", new JSONArray(graphicsProperties.getTileSets().toString()));
			json.put("entityz", graphicsProperties.getEntityZIndex());

			JSONObject playerJson = buildPlayerJson(pokemonTrainer);

			JSONObject playerOverworldJoinJson = new JSONObject(playerJson.toString());
			playerOverworldJoinJson.put("event", "overworld-add-player");

			JSONArray playersArray = new JSONArray();
			json.put("players", playersArray);
			playersArray.put(playerJson);

			synchronized (destinationOverworld) {
				for (String otherPlayerName : destinationOverworld.getPokemonTrainers()) {
					PokemonTrainer otherPokemonTrainer = PokemonTrainer.manager.getByName(otherPlayerName);
					JSONObject otherPlayerJson = buildPlayerJson(otherPokemonTrainer);

					WebsocketConnection otherPlayerConnection = PlayerRegistry.getWebsocketConnection(otherPlayerName);
					otherPlayerConnection.send(playerOverworldJoinJson);
					playersArray.put(otherPlayerJson);
				}

				destinationOverworld.addPokemonTrainer(pokemonTrainer.getName());
			}

			WebsocketConnection playerConnection = PlayerRegistry.getWebsocketConnection(pokemonTrainer.getName());
			playerConnection.send(json);
		}
		else {
			// TODO - teleport inside the same map
		}

	}

	private OverworldLocationProperty buildLocationProperty() {
		OverworldLocationProperty location = new OverworldLocationProperty();

		location.setOverworld(getOverworld());
		location.setX(getX());
		location.setY(getY());
		location.setDirection("down");

		return location;
	}

	public static AvatarsProperty safeGetAvatarsProperty(PokemonTrainer pokemonTrainer) {
		AvatarsProperty avatars = pokemonTrainer.getProperty(AvatarsProperty.class);

		if (avatars == null) {
			avatars = new AvatarsProperty();

			avatars.addAvailableAvatar("default");
			avatars.setAvatar("default");

			pokemonTrainer.addProperty(avatars);
		}

		return avatars;
	}

	private static JSONObject buildPlayerJson(PokemonTrainer pokemonTrainer) {
		JSONObject playerJson = new JSONObject();
		OverworldLocationProperty playerLocation = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		AvatarsProperty avatars = safeGetAvatarsProperty(pokemonTrainer);

		playerJson.put("name", pokemonTrainer.getName());
		playerJson.put("avatar", avatars.getAvatar());
		playerJson.put("x", playerLocation.getX());
		playerJson.put("y", playerLocation.getY());
		playerJson.put("moveSpeed", (String) pokemonTrainer.getProperty("moveSpeed"));

		return playerJson;
	}

	public static class Factory extends ActionFactory {
		@Override
		public String getName() {
			return OverworldTeleportAction.class.getName();
		}

		@Override
		public Action buildAction(String o) throws JPokemonException {
			List<String> options = Options.parseArray(o);
			OverworldTeleportAction ota = new OverworldTeleportAction();

			ota.setOverworld(options.get(0));
			ota.setX(Integer.parseInt(options.get(1)));
			ota.setY(Integer.parseInt(options.get(2)));

			return ota;
		}

		@Override
		public String serializeAction(Action action) throws JPokemonException {
			OverworldTeleportAction ota = (OverworldTeleportAction) action;
			return '[' + ota.getOverworld() + ',' + ota.getX() + ',' + ota.getY() + ']';
		}
	}
}
