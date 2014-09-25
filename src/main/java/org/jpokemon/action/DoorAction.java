package org.jpokemon.action;

import org.jpokemon.api.Action;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.OverworldEntity;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.builder.SingletonBuilder;
import org.jpokemon.movement.Door;

public class DoorAction extends Action {
	@Override
	public void execute(Overworld overworld, OverworldEntity entity, PokemonTrainer pokemonTrainer) {

		Overworld destinationOverworld = Overworld.manager.get(entity.getName());
		OverworldEntity correspondingDoor = null;
		for (OverworldEntity foreignEntity : destinationOverworld.getEntities()) {
			if (foreignEntity.getMovement() instanceof Door
					&& foreignEntity.getName().equals(overworld.getName())) {
				correspondingDoor = foreignEntity;
			}
		}

		if (correspondingDoor != null) {
			OverworldTeleport teleport = new OverworldTeleport();
			teleport.setOverworld(entity.getName());
			teleport.setX(correspondingDoor.getX());
			teleport.setY(correspondingDoor.getY());

			teleport.execute(overworld, entity, pokemonTrainer);
		}
	}

	public static class Builder extends SingletonBuilder<Action> {
		public Builder() {
			super(new DoorAction());
		}
	}
}
