package org.jpokemon.server.event;

import org.jpokemon.api.PokemonTrainer;
import org.zachtaylor.emissary.WebsocketConnection;

public class PokemonTrainerLogin {
	protected final PokemonTrainer pokemonTrainer;
	protected final WebsocketConnection connection;

	public PokemonTrainerLogin(PokemonTrainer pokemonTrainer, WebsocketConnection connection) {
		this.pokemonTrainer = pokemonTrainer;
		this.connection = connection;
	}

	public PokemonTrainer getPokemonTrainer() {
		return pokemonTrainer;
	}

	public WebsocketConnection getConnection() {
		return connection;
	}
}
