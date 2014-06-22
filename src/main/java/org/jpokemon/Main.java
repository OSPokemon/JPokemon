package org.jpokemon;

import org.eclipse.jetty.servlet.ServletHolder;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.api.SimpleAbilityEffectFactoryManager;
import org.jpokemon.api.SimpleAbilityManager;
import org.jpokemon.api.SimpleBattleEffectFactoryManager;
import org.jpokemon.api.SimpleContestCategoryManager;
import org.jpokemon.api.SimpleEvolutionFactoryManager;
import org.jpokemon.api.SimpleExperienceCurveManager;
import org.jpokemon.api.SimpleItemAttributeFactoryManager;
import org.jpokemon.api.SimpleItemManager;
import org.jpokemon.api.SimpleMoveManager;
import org.jpokemon.api.SimpleMovePropertyFactoryManager;
import org.jpokemon.api.SimpleNatureManager;
import org.jpokemon.api.SimplePokemonManager;
import org.jpokemon.api.SimplePokemonMetaDataFactoryManager;
import org.jpokemon.api.SimplePokemonTrainerManager;
import org.jpokemon.api.SimpleSpeciesManager;
import org.jpokemon.api.SimpleStatusConditionManager;
import org.jpokemon.api.SimpleTargetingSchemeManager;
import org.jpokemon.api.SimpleTrainerClassManager;
import org.jpokemon.api.SimpleTrainerMetaDataFactory;
import org.jpokemon.api.SimpleTypeManager;
import org.jpokemon.api.TrainerMetaDataFactory;
import org.jpokemon.server.JPokemonServer;
import org.jpokemon.server.JPokemonServlet;
import org.jpokemon.server.PasswordData;
import org.jpokemon.server.PlayerRegistry;
import org.jpokemon.server.RoleData;

public class Main {
	protected static JPokemonServer server;

	public static void main(String[] args) throws Exception {
		server = new JPokemonServer();

		configureApi();
		configureServer();

		server.start();
		server.join();
	}

	public static void configureApi() {
		// Initialize all simple managers
		SimpleAbilityManager.init();
		SimpleAbilityEffectFactoryManager.init();
		SimpleBattleEffectFactoryManager.init();
		SimpleContestCategoryManager.init();
		SimpleEvolutionFactoryManager.init();
		SimpleExperienceCurveManager.init();
		SimpleItemManager.init();
		SimpleItemAttributeFactoryManager.init();
		SimpleMoveManager.init();
		SimpleMovePropertyFactoryManager.init();
		SimpleNatureManager.init();
		SimplePokemonManager.init();
		SimplePokemonMetaDataFactoryManager.init();
		SimplePokemonTrainerManager.init();
		SimpleSpeciesManager.init();
		SimpleStatusConditionManager.init();
		SimpleTargetingSchemeManager.init();
		SimpleTrainerClassManager.init();
		SimpleTrainerMetaDataFactory.init();
		SimpleTypeManager.init();

		PlayerRegistry.guaranteeClassLoad();

		TrainerMetaDataFactory.manager.register(new PasswordData.Factory());
		TrainerMetaDataFactory.manager.register(new RoleData.Factory());
	}

	public static void configureServer() {
		server.getContext().addServlet(new ServletHolder(new JPokemonServlet()), "/*");

		PokemonTrainer adminTrainer = new PokemonTrainer();
		adminTrainer.setName("admin");
		PasswordData passwordData = new PasswordData();
		passwordData.setPassword("admin");
		adminTrainer.addMetaData(passwordData);
		RoleData roleData = new RoleData();
		roleData.addRole("user");
		roleData.addRole("admin");
		adminTrainer.addMetaData(roleData);
		PokemonTrainer.manager.register(adminTrainer);

		PokemonTrainer userTrainer = new PokemonTrainer();
		userTrainer.setName("user");
		passwordData = new PasswordData();
		passwordData.setPassword("user");
		userTrainer.addMetaData(passwordData);
		roleData = new RoleData();
		roleData.addRole("user");
		userTrainer.addMetaData(roleData);
		PokemonTrainer.manager.register(userTrainer);
	}
}
