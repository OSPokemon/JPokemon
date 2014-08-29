package org.jpokemon;

import org.eclipse.jetty.servlet.ServletHolder;
import org.jpokemon.action.DoorAction;
import org.jpokemon.action.OverworldTeleportAction;
import org.jpokemon.action.SpawnWildBattleAction;
import org.jpokemon.action.SpeechAction;
import org.jpokemon.api.Ability;
import org.jpokemon.api.ActionFactory;
import org.jpokemon.api.ContestCategory;
import org.jpokemon.api.ExperienceCurve;
import org.jpokemon.api.Item;
import org.jpokemon.api.Move;
import org.jpokemon.api.MovementScheme;
import org.jpokemon.api.Nature;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.Pokemon;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.api.PropertyProvider;
import org.jpokemon.api.Species;
import org.jpokemon.api.StatusCondition;
import org.jpokemon.api.TargetingScheme;
import org.jpokemon.api.TrainerClass;
import org.jpokemon.api.Type;
import org.jpokemon.manager.ClassicExperienceCurveManager;
import org.jpokemon.manager.ClassicTypeManager;
import org.jpokemon.manager.HashedCachingManager;
import org.jpokemon.manager.SimpleManager;
import org.jpokemon.manager.SimpleMovementSchemeManager;
import org.jpokemon.manager.SimplePokemonManager;
import org.jpokemon.manager.TmxFileOverworldManager;
import org.jpokemon.property.trainer.AvatarsProperty;
import org.jpokemon.property.trainer.OverworldLocationProperty;
import org.jpokemon.property.trainer.server.UserIdentityProperty;
import org.jpokemon.server.FileServlet;
import org.jpokemon.server.JPokemonServer;
import org.jpokemon.server.JPokemonWebsocketServlet;
import org.jpokemon.server.PlayerRegistry;
import org.jpokemon.server.rest.JPokemonServletHolder;

public class Main {
	protected static JPokemonServer server;

	public static void main(String[] args) throws Exception {
		server = new JPokemonServer();

		configureApi();
		configureServer();
		configureServlets();
		server.configureSecurity();

		server.start();
		server.join();
	}

	public static void configureApi() {
		// Initialize all simple managers
		Ability.manager = new SimpleManager<Ability>(Ability.class);
		ActionFactory.manager = new SimpleManager<ActionFactory>(ActionFactory.class);
		// BattleEffectFactory.manager = new
		// SimpleManager<BattleEffectFactory>(BattleEffectFactory.class);
		ContestCategory.manager = new SimpleManager<ContestCategory>(ContestCategory.class);
		// EvolutionFactory.manager = new ClassicEvolutionFactoryManager();
		ExperienceCurve.manager = new ClassicExperienceCurveManager();
		Item.manager = new SimpleManager<Item>(Item.class);
		Move.manager = new SimpleManager<Move>(Move.class);
		MovementScheme.manager = new SimpleMovementSchemeManager();
		Nature.manager = new SimpleManager<Nature>(Nature.class);
		Overworld.manager = new HashedCachingManager<Overworld>(new TmxFileOverworldManager("src/main/www/map"));
		Pokemon.manager = new SimplePokemonManager();
		PokemonTrainer.manager = new SimpleManager<PokemonTrainer>(PokemonTrainer.class);
		Species.manager = new SimpleManager<Species>(Species.class);
		StatusCondition.manager = new SimpleManager<StatusCondition>(StatusCondition.class);
		TargetingScheme.manager = new SimpleManager<TargetingScheme>(TargetingScheme.class);
		TrainerClass.manager = new SimpleManager<TrainerClass>(TrainerClass.class);
		Type.manager = new ClassicTypeManager();

		PlayerRegistry.guaranteeClassLoad();

		PropertyProvider.register(new UserIdentityProperty.Provider());
		PropertyProvider.register(new AvatarsProperty.Provider());
		PropertyProvider.register(new OverworldLocationProperty.Provider());

		ActionFactory.manager.register(new DoorAction.Factory());
		ActionFactory.manager.register(new OverworldTeleportAction.Factory());
		ActionFactory.manager.register(new SpeechAction.Factory());
	}

	public static void configureServer() {
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
		userTrainer.addProperty(userIdentity);
		PokemonTrainer.manager.register(userTrainer);
	}

	public static void configureServlets() {
		server.getContext().addServlet(new JPokemonServletHolder(), "/api/*");
		server.getContext().addServlet(new ServletHolder(new JPokemonWebsocketServlet()), "/ws");
		// FileServlet from jpokemon-server-lib defaultly serves accessories to that
		// library, e.g. the Admin pages, which are not in its' ```main``` dir
		// This project is all about the website, so, it's in ```main``` here
		server.getContext().addServlet(new ServletHolder(new FileServlet("src/main/www")), "/*");
	}
}
