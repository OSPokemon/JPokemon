package org.jpokemon;

import org.eclipse.jetty.servlet.ServletHolder;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.api.PropertyProvider;
import org.jpokemon.manager.ClassicEvolutionFactoryManager;
import org.jpokemon.manager.ClassicExperienceCurveManager;
import org.jpokemon.manager.ClassicTypeManager;
import org.jpokemon.manager.SimpleAbilityManager;
import org.jpokemon.manager.SimpleBattleEffectFactoryManager;
import org.jpokemon.manager.SimpleContestCategoryManager;
import org.jpokemon.manager.SimpleItemManager;
import org.jpokemon.manager.SimpleMoveManager;
import org.jpokemon.manager.SimpleNatureManager;
import org.jpokemon.manager.SimplePokemonManager;
import org.jpokemon.manager.SimplePokemonTrainerManager;
import org.jpokemon.manager.SimpleSpeciesManager;
import org.jpokemon.manager.SimpleStatusConditionManager;
import org.jpokemon.manager.SimpleTargetingSchemeManager;
import org.jpokemon.manager.SimpleTrainerClassManager;
import org.jpokemon.manager.TmxFileOverworldManager;
import org.jpokemon.property.trainer.AvatarsProperty;
import org.jpokemon.property.trainer.OverworldLocationProperty;
import org.jpokemon.server.JPokemonServer;
import org.jpokemon.server.JPokemonServlet;
import org.jpokemon.server.PlayerRegistry;
import org.jpokemon.server.UserIdentityProperty;

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
		SimpleBattleEffectFactoryManager.init();
		SimpleContestCategoryManager.init();
		ClassicEvolutionFactoryManager.init();
		ClassicExperienceCurveManager.init();
		SimpleItemManager.init();
		SimpleMoveManager.init();
		SimpleNatureManager.init();
		TmxFileOverworldManager.init("src/main/www/map");
		SimplePokemonManager.init();
		SimplePokemonTrainerManager.init();
		SimpleSpeciesManager.init();
		SimpleStatusConditionManager.init();
		SimpleTargetingSchemeManager.init();
		SimpleTrainerClassManager.init();
		ClassicTypeManager.init();

		PlayerRegistry.guaranteeClassLoad();

		PropertyProvider.register(new UserIdentityProperty.Provider());
		PropertyProvider.register(new AvatarsProperty.Provider());
		PropertyProvider.register(new OverworldLocationProperty.Provider());
	}

	public static void configureServer() {
		server.getContext().addServlet(new ServletHolder(new JPokemonServlet()), "/*");

		// Build the default admin
		PokemonTrainer adminTrainer = new PokemonTrainer();
		adminTrainer.setName("admin");
		UserIdentityProperty adminIdentity = new UserIdentityProperty();
		adminIdentity.setPassword("admin");
		adminIdentity.addRole("user");
		adminIdentity.addRole("admin");
		adminTrainer.addProperty(adminIdentity);
		PokemonTrainer.manager.register(adminTrainer);

		// Build the default user
		PokemonTrainer userTrainer = new PokemonTrainer();
		userTrainer.setName("user");
		UserIdentityProperty userIdentity = new UserIdentityProperty();
		userIdentity.setPassword("user");
		userIdentity.addRole("user");
		adminTrainer.addProperty(userIdentity);
		PokemonTrainer.manager.register(userTrainer);
	}
}
