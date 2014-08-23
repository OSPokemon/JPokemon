package org.jpokemon.action;

import org.jpokemon.api.Action;
import org.jpokemon.api.ActionFactory;
import org.jpokemon.api.JPokemonException;
import org.jpokemon.api.PokemonTrainer;

public class SpeechAction implements Action {
	protected String speechId;

	public String getSpeechId() {
		return speechId;
	}

	public void setSpeechId(String speechId) {
		this.speechId = speechId;
	}

	@Override
	public void execute(PokemonTrainer pokemonTrainer) throws JPokemonException {
		// TODO Auto-generated method stub
	}

	public static class Factory extends ActionFactory {
		@Override
		public String getName() {
			return SpeechAction.class.getName();
		}

		@Override
		public Action buildAction(String options) throws JPokemonException {
			SpeechAction speechAction = new SpeechAction();
			speechAction.setSpeechId(options);
			return speechAction;
		}

		@Override
		public String serializeAction(Action action) throws JPokemonException {
			SpeechAction speechAction = (SpeechAction) action;
			return speechAction.getSpeechId();
		}
	}
}
