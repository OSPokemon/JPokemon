package org.jpokemon.server;

import java.util.ArrayList;
import java.util.List;

import org.jpokemon.api.Action;
import org.jpokemon.api.ActionSet;
import org.jpokemon.api.MovementScheme;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.OverworldEntity;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.api.Requirement;
import org.jpokemon.property.overworld.TmxFileProperties;
import org.jpokemon.property.trainer.AvatarsProperty;
import org.jpokemon.property.trainer.OverworldLocationProperty;
import org.jpokemon.server.event.PokemonTrainerLogin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zachtaylor.emissary.Emissary;
import org.zachtaylor.emissary.WebsocketConnection;
import org.zachtaylor.emissary.event.WebsocketConnectionClose;

public class OverworldEmissary extends Emissary {
	public static final OverworldEmissary instance = new OverworldEmissary();

	private OverworldEmissary() {
		register(PokemonTrainerLogin.class, this);
		register(WebsocketConnectionClose.class, this);
	}

	@Override
	public void serve(WebsocketConnection connection, JSONObject json) {
		String action = json.getString("action");

		if ("move".equals(action)) {
			move(connection, json);
		}
		else if ("look".equals(action)) {
			look(connection, json);
		}
		else if ("interact".equals(action)) {
			interact(connection, json);
		}
	}

	public void handle(PokemonTrainerLogin event) {
		PokemonTrainer pokemonTrainer = event.getPokemonTrainer();
		OverworldLocationProperty locationProperty = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		AvatarsProperty avatarsProperty = pokemonTrainer.getProperty(AvatarsProperty.class);

		if (locationProperty == null) {
			locationProperty = new OverworldLocationProperty();

			// TODO - make this customizable
			locationProperty.setOverworld("bedroom");
			locationProperty.setX(10);
			locationProperty.setY(5);
			locationProperty.setDirection("down");

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
		mapJson.put("entityz", overworld.getProperty(TmxFileProperties.class).getEntityZIndex());

		JSONObject playerJson = new JSONObject();
		playerJson.put("name", pokemonTrainer.getName());
		playerJson.put("avatar", avatarsProperty.getAvatar());
		playerJson.put("x", locationProperty.getX());
		playerJson.put("y", locationProperty.getY());
		playerJson.put("moveSpeed", (String) pokemonTrainer.getProperty("moveSpeed"));

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
				otherPlayerJson.put("moveSpeed", (String) otherPokemonTrainer.getProperty("moveSpeed"));

				WebsocketConnection otherPlayerConnection = PlayerRegistry.getWebsocketConnection(otherPlayerName);
				otherPlayerConnection.send(playerJson);
				playersArray.put(otherPlayerJson);
			}

			mapJson.put("players", playersArray);
			event.getConnection().send(mapJson);

			overworld.addPokemonTrainer(pokemonTrainer.getName());
		}
	}

	public void handle(WebsocketConnectionClose event) {
		String name = event.getWebsocket().getName();

		if (name == null) {
			return;
		}

		PokemonTrainer pokemonTrainer = PokemonTrainer.manager.getByName(name);
		OverworldLocationProperty locationProperty = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		Overworld overworld = Overworld.manager.getByName(locationProperty.getOverworld());

		synchronized (overworld) {
			overworld.removePokemonTrainer(name);
		}
	}

	public void move(WebsocketConnection connection, JSONObject json) {
		String name = connection.getName();
		PokemonTrainer pokemonTrainer = PokemonTrainer.manager.getByName(name);

		if (!_checkClock(pokemonTrainer)) {
			return;
		}
		_recordClock(pokemonTrainer);

		String direction = json.getString("direction");
		OverworldLocationProperty location = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		Overworld overworld = Overworld.manager.getByName(location.getOverworld());

		OverworldLocationProperty nextLocation = _locationInDirection(overworld, location, direction);

		if (nextLocation == null) {
			return;
		}

		List<OverworldEntity> entitiesInside = _getEntitiesAt(overworld, location);
		List<OverworldEntity> entitiesNext = _getEntitiesAt(overworld, location);

		for (OverworldEntity overworldEntity : entitiesInside) {
			MovementScheme movementScheme = MovementScheme.manager.getByName(overworldEntity.getMovement());

			if (movementScheme == null) {
				continue;
			}

			if (!movementScheme.canExitToward(direction)) {
				return;
			}
		}

		String oppositeDirection = _oppositeDirection(direction);

		for (OverworldEntity overworldEntity : entitiesNext) {
			MovementScheme movementScheme = MovementScheme.manager.getByName(overworldEntity.getMovement());

			if (movementScheme == null) {
				continue;
			}

			if (!movementScheme.canEnterFrom(oppositeDirection)) {
				return;
			}
		}

		location.setX(nextLocation.getX());
		location.setY(nextLocation.getY());

		JSONObject updateJson = new JSONObject();
		updateJson.put("event", "overworld-move");
		updateJson.put("name", name);
		updateJson.put("animation", "walk" + direction);
		updateJson.put("x", nextLocation.getX());
		updateJson.put("y", nextLocation.getY());

		synchronized (overworld) {
			for (String playerName : overworld.getPokemonTrainers()) {
				WebsocketConnection playerConnection = PlayerRegistry.getWebsocketConnection(playerName);
				playerConnection.send(updateJson);
			}
		}
	}

	public void look(WebsocketConnection connection, JSONObject json) {
		String name = connection.getName();
		PokemonTrainer pokemonTrainer = PokemonTrainer.manager.getByName(name);

		if (!_checkClock(pokemonTrainer)) {
			return;
		}
		// Don't record clock here

		String direction = json.getString("direction");

		if (_oppositeDirection(direction) == null) { // direction was invalid
			return;
		}

		OverworldLocationProperty location = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		Overworld overworld = Overworld.manager.getByName(location.getOverworld());

		JSONObject updateJson = new JSONObject();
		updateJson.put("event", "overworld-look");
		updateJson.put("name", name);
		updateJson.put("direction", direction);

		synchronized (overworld) {
			for (String playerName : overworld.getPokemonTrainers()) {
				WebsocketConnection playerConnection = PlayerRegistry.getWebsocketConnection(playerName);
				playerConnection.send(updateJson);
			}
		}
	}

	public void interact(WebsocketConnection connection, JSONObject json) {
		String name = connection.getName();
		PokemonTrainer pokemonTrainer = PokemonTrainer.manager.getByName(name);

		if (!_checkClock(pokemonTrainer)) {
			return;
		}
		// Don't record clock here

		String direction = json.getString("direction");
		OverworldLocationProperty location = pokemonTrainer.getProperty(OverworldLocationProperty.class);
		Overworld overworld = Overworld.manager.getByName(location.getOverworld());

		OverworldLocationProperty nextLocation = _locationInDirection(overworld, location, direction);

		if (nextLocation == null) {
			return;
		}

		List<OverworldEntity> entities = _getEntitiesAt(overworld, nextLocation);

		List<String> entitiesWithInteractOptions = new ArrayList<String>();

		for (OverworldEntity entity : entities) {
			List<ActionSet> actionSets = entity.getActionSets("interact");

			if (actionSets != null && actionSets.size() > 0) {
				entitiesWithInteractOptions.add(entity.getName());
			}
		}

		if (entitiesWithInteractOptions.size() > 1) {
			// TODO - send question to which entity to interact to i guess
			return;
		}
		if (entitiesWithInteractOptions.size() < 1) {
			return;
		}

		OverworldEntity entity = null;
		for (OverworldEntity e : entities) {
			if (entitiesWithInteractOptions.contains(e.getName())) {
				entity = e;
				break;
			}
		}

		List<ActionSet> actionSets = entity.getActionSets("interact");

		if (actionSets.size() > 1) {
			// TODO - send question to have a chat choice i guess
			return;
		}

		ActionSet actionSet = actionSets.get(0);

		for (Requirement requirement : actionSet.getRequirements()) {
			if (!requirement.isSatisfied(pokemonTrainer)) {
				return;
			}
		}

		for (Action action : actionSet.getActions()) {
			action.execute(pokemonTrainer);
		}
	}

	private boolean _checkClock(PokemonTrainer pokemonTrainer) {
		long moveSpeed = _safeGetMoveSpeed(pokemonTrainer);
		long moveTime = _safeGetMoveTime(pokemonTrainer);
		long timeNow = System.currentTimeMillis();

		if (timeNow < moveTime - (moveSpeed / 4)) {
			// Allow to submit the next move if you are 75% of the way moved
			// helps to decrease stutter
			return false;
		}

		return true;
	}

	private void _recordClock(PokemonTrainer pokemonTrainer) {
		long moveSpeed = _safeGetMoveSpeed(pokemonTrainer);
		long moveTime = _safeGetMoveTime(pokemonTrainer);
		long timeNow = System.currentTimeMillis();

		moveTime = Math.max(moveTime, timeNow) + moveSpeed;
		pokemonTrainer.setProperty("moveTime", moveTime + "");
	}

	private List<OverworldEntity> _getEntitiesAt(Overworld overworld, OverworldLocationProperty location) {
		List<OverworldEntity> overworldEntities = new ArrayList<OverworldEntity>();

		for (OverworldEntity overworldEntity : overworld.getEntities()) {
			if (overworldEntity.getX() <= location.getX()
					&& overworldEntity.getX() + overworldEntity.getWidth() - 1 >= location.getX()
					&& overworldEntity.getY() <= location.getY()
					&& overworldEntity.getY() + overworldEntity.getWidth() - 1 >= location.getY()) {

				overworldEntities.add(overworldEntity);

			}
		}

		return overworldEntities;
	}

	private OverworldLocationProperty _locationInDirection(Overworld overworld, OverworldLocationProperty location,
			String direction) {
		OverworldLocationProperty nextLocation = new OverworldLocationProperty();

		nextLocation.setOverworld(location.getOverworld());
		nextLocation.setX(location.getX());
		nextLocation.setY(location.getY());

		if ("up".equals(direction)) {
			if (location.getY() < 1) {
				nextLocation = null;
			}
			else {
				nextLocation.setY(location.getY() - 1);
			}
		}
		else if ("left".equals(direction)) {
			if (location.getX() < 1) {
				nextLocation = null;
			}
			else {
				nextLocation.setX(location.getX() - 1);
			}
		}
		else if ("down".equals(direction)) {
			if (overworld.getHeight() - 1 == location.getY()) {
				nextLocation = null;
			}
			else {
				nextLocation.setY(overworld.getHeight() - 1);
			}
		}
		else if ("right".equals(direction)) {
			if (overworld.getWidth() - 1 == location.getX()) {
				nextLocation = null;
			}
			else {
				nextLocation.setX(location.getX() + 1);
			}
		}
		else { // Invalid move
			nextLocation = null;
		}

		return nextLocation;
	}

	private Long _safeGetMoveSpeed(PokemonTrainer pokemonTrainer) {
		Object moveSpeedString = pokemonTrainer.getProperty("moveSpeed");

		if (moveSpeedString == null) {
			// TODO - base move speed should be a setting or something
			pokemonTrainer.setProperty("moveSpeed", moveSpeedString = "500");
		}

		return Long.parseLong((String) moveSpeedString);
	}

	private Long _safeGetMoveTime(PokemonTrainer pokemonTrainer) {
		Object moveTimeString = pokemonTrainer.getProperty("moveTime");

		if (moveTimeString == null) {
			// The truth is it doesn't matter what this value is
			// Anything significantly < timeNow should trigger a move
			pokemonTrainer.setProperty("moveTime", moveTimeString = "0");
		}

		return Long.parseLong((String) moveTimeString);
	}

	private String _oppositeDirection(String direction) {
		if ("up".equals(direction)) {
			return "down";
		}
		if ("left".equals(direction)) {
			return "right";
		}
		if ("down".equals(direction)) {
			return "up";
		}
		else if ("right".equals(direction)) {
			return "left";
		}

		return null;
	}
}
