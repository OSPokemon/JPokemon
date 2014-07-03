Emissary.defineController('org.jpokemon.overworld.PokemonTrainer', {
  view: false,
  name: null,

  constructor: function(playerJson) {
    this.name = playerJson.name;
  }
});
