Emissary.defineController('org.jpokemon.admin.Ability', {
  abilityTable: '.table',
  authorizationHeader: null,

  constructor: function(authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
  },

  update: function() {
    $.get('api/ability', null, function(abilities) {
      this.abilityTable.empty();

      $('<tr><th>Sync</th><th>Name</th><th>Description</th><th>Effects</th></tr>').appendTo(this.abilityTable);

      $.each(abilities, function(index, ability) {
        var abilityRow = new (Emissary.getController('org.jpokemon.admin.AbilityRow'))(ability, this);
        abilityRow.view.appendTo(this.abilityTable);
      }.bind(this));

      var addAbilityRow = new (Emissary.getController('org.jpokemon.admin.AbilityRow'))(null, this);
      addAbilityRow.view.appendTo(this.abilityTable);
    }.bind(this));
  }
});
