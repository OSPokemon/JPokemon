Emissary.defineController('org.jpokemon.admin.AbilityRow', {
  glyphicon: '.glyphicon',
  name: '.ability-name',
  description: '.ability-description',
  effects: '.ability-effects',
  ability: null,
  abilityController: null,

  constructor: function(ability, abilityController) {
    console.log('new AbilityRow');

    this.ability = ability;
    this.abilityController = abilityController;

    if (this.ability) {
      this.glyphicon.addClass('glyphicon-floppy-disk');
      this.name.val(this.ability.name);
      this.description.val(this.ability.description);
      this.effects.html(this.ability.effects);
    }
    else {
      this.glyphicon.addClass('glyphicon-floppy-open');
    }

    this.glyphicon.click(this.glyphiconClick.bind(this));
  },

  glyphiconClick: function() {
    var newName = this.name.val(),
        newDescription = this.description.val();

    if (this.ability) {

    }
    else {
      var newAbility = {
        name: newName,
        description: newDescription,
        effects: [
        ]
      };

      $.ajax({
        type: 'POST',
        url: 'api/ability',
        data: JSON.stringify(newAbility),
        contentType: 'application/json',
        success: function(data, textStatus, jqXHR) {
          console.log('success');
          console.log(arguments);
        }.bind(this),
        error: function(jqXHR, textStatus, errorThrown) {
          console.log('error');
          console.log(arguments);
        }.bind(this)
      });
    }
  }
});
