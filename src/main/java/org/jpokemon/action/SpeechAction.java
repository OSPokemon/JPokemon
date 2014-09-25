package org.jpokemon.action;

import org.jpokemon.api.Action;
import org.jpokemon.api.JPokemonException;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.OverworldEntity;
import org.jpokemon.api.PokemonTrainer;

public class SpeechAction extends Action {
	protected String speechId;

	public String getSpeechId() {
		return speechId;
	}

	public void setSpeechId(String speechId) {
		this.speechId = speechId;
	}

	@Override
	public void execute(Overworld overworld, OverworldEntity entity,  PokemonTrainer pokemonTrainer) {
		// TODO Auto-generated method stub
	}

	public static class Builder implements org.jpokemon.api.Builder<Action> {
		@Override
		public String getId() {
			return SpeechAction.class.getName();
		}

		@Override
		public Action construct(String options) throws JPokemonException {
			SpeechAction speechAction = new SpeechAction();
			speechAction.setSpeechId(options);
			return speechAction;
		}

		@Override
		public String destruct(Action action) throws JPokemonException {
			SpeechAction speechAction = (SpeechAction) action;
			return speechAction.getSpeechId();
		}
	}
}
