package org.jpokemon;

import org.eclipse.jetty.servlet.ServletHolder;
import org.jpokemon.action.DoorAction;
import org.jpokemon.action.OverworldTeleport;
import org.jpokemon.action.SpeechAction;
import org.jpokemon.api.Ability;
import org.jpokemon.api.Action;
import org.jpokemon.api.ActionSet;
import org.jpokemon.api.BattleEffect;
import org.jpokemon.api.ContestCategory;
import org.jpokemon.api.ExperienceCurve;
import org.jpokemon.api.Item;
import org.jpokemon.api.Move;
import org.jpokemon.api.MovementScheme;
import org.jpokemon.api.Nature;
import org.jpokemon.api.Overworld;
import org.jpokemon.api.Pokemon;
import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.api.Property;
import org.jpokemon.api.Species;
import org.jpokemon.api.StatusCondition;
import org.jpokemon.api.TargetingScheme;
import org.jpokemon.api.TrainerClass;
import org.jpokemon.api.Type;
import org.jpokemon.manager.ClassicExperienceCurveManager;
import org.jpokemon.manager.ClassicTypeManager;
import org.jpokemon.manager.HashedCachingManager;
import org.jpokemon.manager.SimpleBuildersManager;
import org.jpokemon.manager.SimpleManager;
import org.jpokemon.manager.SimpleMovementSchemeManager;
import org.jpokemon.manager.TmxFileOverworldManager;
import org.jpokemon.property.trainer.Avatars;
import org.jpokemon.property.trainer.OverworldLocation;
import org.jpokemon.property.trainer.server.ServerIdentity;
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
		Action.builders = new SimpleBuildersManager<Action>();
		ActionSet.manager = new SimpleManager<ActionSet>(ActionSet.class);
		BattleEffect.builders = new SimpleBuildersManager<BattleEffect>();
		ContestCategory.manager = new SimpleManager<ContestCategory>(ContestCategory.class);
		ExperienceCurve.manager = new ClassicExperienceCurveManager();
		Item.manager = new SimpleManager<Item>(Item.class);
		Move.manager = new SimpleManager<Move>(Move.class);
		MovementScheme.builders = new SimpleMovementSchemeManager();
		Nature.manager = new SimpleManager<Nature>(Nature.class);
		Overworld.manager = new HashedCachingManager<Overworld>(new TmxFileOverworldManager("src/main/www/map"));
		Pokemon.manager = new SimpleManager<Pokemon>(Pokemon.class);
		PokemonTrainer.manager = new SimpleManager<PokemonTrainer>(PokemonTrainer.class);
		Property.builders = new SimpleBuildersManager<Object>();
		Species.manager = new SimpleManager<Species>(Species.class);
		StatusCondition.manager = new SimpleManager<StatusCondition>(StatusCondition.class);
		TargetingScheme.manager = new SimpleManager<TargetingScheme>(TargetingScheme.class);
		TrainerClass.manager = new SimpleManager<TrainerClass>(TrainerClass.class);
		Type.manager = new ClassicTypeManager();

		PlayerRegistry.guaranteeClassLoad();

		Property.builders.register(new ServerIdentity.Builder());
		Property.builders.register(new Avatars.Builder());
		Property.builders.register(new OverworldLocation.Builder());

		Action.builders.register(new DoorAction.Builder());
		Action.builders.register(new OverworldTeleport.Builder());
		Action.builders.register(new SpeechAction.Builder());
	}

	public static void configureServer() {
		// Build the default admin
		PokemonTrainer adminTrainer = new PokemonTrainer();
		adminTrainer.setId("admin");
		adminTrainer.setName("admin");
		ServerIdentity adminIdentity = new ServerIdentity();
		adminIdentity.setPassword("admin");
		adminIdentity.addRole("user");
		adminIdentity.addRole("admin");
		adminTrainer.setProperty(ServerIdentity.class, adminIdentity);
		PokemonTrainer.manager.register(adminTrainer);

		// Build the default user
		PokemonTrainer userTrainer = new PokemonTrainer();
		userTrainer.setId("user");
		userTrainer.setName("user");
		ServerIdentity userIdentity = new ServerIdentity();
		userIdentity.setPassword("user");
		userIdentity.addRole("user");
		userTrainer.setProperty(ServerIdentity.class, userIdentity);
		PokemonTrainer.manager.register(userTrainer);

		// Build the default user
		PokemonTrainer verenaTrainer = new PokemonTrainer();
		verenaTrainer.setId("verena");
		verenaTrainer.setName("verena");
		ServerIdentity verenaIdentity = new ServerIdentity();
		verenaIdentity.setPassword("verena");
		verenaIdentity.addRole("user");
		verenaTrainer.setProperty(ServerIdentity.class, verenaIdentity);
		PokemonTrainer.manager.register(verenaTrainer);
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
