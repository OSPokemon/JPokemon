package org.jpokemon.action;

import java.util.Map;

import org.jpokemon.api.Action;
import org.jpokemon.api.Battle;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.OverworldEntity;
import org.jpokemon.api.Pokemon;
import org.jpokemon.api.PokemonContainer;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.api.TrainerContainer;
import org.jpokemon.property.overworldentity.PokemonSpawnProperty;
import org.jpokemon.util.Macro;

public class SpawnWildBattleAction extends Action {
	@Override
	public void execute(Overworld overworld, OverworldEntity entity, PokemonTrainer pokemonTrainer) {
		PokemonSpawnProperty spawnProperties = entity.getProperty(PokemonSpawnProperty.class);

		if (spawnProperties == null) {
			return;
		}

		TrainerContainer trainerContainer = new TrainerContainer();
		trainerContainer.setPokemonTrainer(pokemonTrainer);

		for (Pokemon pokemon : pokemonTrainer.getPokemon()) {
			PokemonContainer pokemonContainer = Macro.buildPokemonContainerWithPokemon(pokemon);
			trainerContainer.getPokemonContainers().add(pokemonContainer);
		}

		TrainerContainer emptyOpponentContainer = new TrainerContainer();

		String species = null;
		int totalWeight = 0;
		for (Map.Entry<String, Integer> speciesEntry : spawnProperties.getSpecies().entrySet()) {
			totalWeight += speciesEntry.getValue();
		}
		int luckyNumber = (int) (Math.random() * totalWeight) + 1;
		for (Map.Entry<String, Integer> speciesEntry : spawnProperties.getSpecies().entrySet()) {
			if (luckyNumber >= speciesEntry.getValue()) {
				species = speciesEntry.getKey();
				break;
			}
			luckyNumber -= speciesEntry.getValue();
		}

		int wildPokemonLevel = spawnProperties.getMinLevel()
				+ (int) (Math.random() * (spawnProperties.getMaxLevel() - spawnProperties.getMinLevel()));

		Pokemon wildPokemon = Macro.buildPokemonWithSpeciesAndLevel(species, wildPokemonLevel);

		PokemonContainer wildPokemonContainer = Macro.buildPokemonContainerWithPokemon(wildPokemon);

		emptyOpponentContainer.addPokemonContainer(wildPokemonContainer);

		Battle battle = new Battle();

		battle.addTrainerContainer(trainerContainer);
		battle.addTrainerContainer(emptyOpponentContainer);

		// the battle is built!

		// TODO now comes the part where you access the network and
		// affect player connection state and stuff like that
	}
}
