package org.jpokemon.server.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jpokemon.api.PokemonTrainer;
import org.jpokemon.server.UserIdentityProperty;
import org.jpokemon.server.rest.resource.SignupResource;

@Path("signup")
public class SignupService {
	@Context
	UriInfo uriInfo;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doSignup(SignupResource signupResource) {
		PokemonTrainer existingPokemonTrainer = PokemonTrainer.manager.getByName(signupResource.getName());

		if (existingPokemonTrainer != null) {
			return Response.serverError().build();
		}

		PokemonTrainer pokemonTrainer = new PokemonTrainer();
		pokemonTrainer.setName(signupResource.getName());
		UserIdentityProperty userIdentityProperty = new UserIdentityProperty();
		userIdentityProperty.setPassword(signupResource.getPassword());
		userIdentityProperty.addRole("user");
		pokemonTrainer.addProperty(userIdentityProperty);
		PokemonTrainer.manager.register(pokemonTrainer);

		return Response.created(uriInfo.getBaseUri().resolve("pokemontrainer/" + signupResource.getName())).build();
	}
}
