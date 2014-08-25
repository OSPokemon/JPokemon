Emissary.defineController('org.jpokemon.Main', {
  navItems: '.nav.navbar-nav',
  
  constructor: function() {
    JPokemon = {
      NavBar: this,
      players: {},
      player: null
    };

    $.websocket.init();

    new (Emissary.getController('org.jpokemon.Login'))();
    new (Emissary.getController('org.jpokemon.overworld.Overworld'))();

    this.loadDriver();
  },

  loadDriver: function() {
    // TODO - check the user agent or something
    new (Emissary.getController('org.jpokemon.driver.Keyboard'))();

    new (Emissary.getController('org.jpokemon.driver.InputReader'))();
  },

  attachNavItem: function(name, callback, dropdown) {
    var navItem = new (Emissary.getController('org.jpokemon.NavItem'))(name);

    if (callback) {
      navItem.addListener(callback);
    }
    else if (dropdown) {
      $.each(dropdown, function(index, row) {
        navItem.addDropdownListener(row.name, row.callback);
      });
    }

    navItem.view.appendTo(this.navItems);
    return navItem;
  }
});
