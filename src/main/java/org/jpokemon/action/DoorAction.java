package org.jpokemon.action;

import org.jpokemon.api.Action;
import org.jpokemon.api.ActionFactory;
import org.jpokemon.api.ActionSet;
import org.jpokemon.api.JPokemonException;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.OverworldEntity;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.movement.Door;

public class DoorAction implements Action {
	@Override
	public void execute(Overworld overworld, OverworldEntity entity, ActionSet actionSet, PokemonTrainer pokemonTrainer) {

		Overworld destinationOverworld = Overworld.manager.getByName(entity.getName());
		OverworldEntity correspondingDoor = null;
		for (OverworldEntity foreignEntity : destinationOverworld.getEntities()) {
			if (foreignEntity.getMovement().equals(Door.class.getName())
					&& foreignEntity.getName().equals(overworld.getName())) {
				correspondingDoor = foreignEntity;
			}
		}

		if (correspondingDoor != null) {
			OverworldTeleportAction teleport = new OverworldTeleportAction();
			teleport.setOverworld(entity.getName());
			teleport.setX(correspondingDoor.getX());
			teleport.setY(correspondingDoor.getY());

			teleport.execute(overworld, entity, actionSet, pokemonTrainer);
		}
	}

	public static class Factory extends ActionFactory {
		@Override
		public String getName() {
			return DoorAction.class.getName();
		}

		@Override
		public Action buildAction(String options) throws JPokemonException {
			return new DoorAction();
		}

		@Override
		public String serializeAction(Action action) throws JPokemonException {
			return null;
		}
	}
}
