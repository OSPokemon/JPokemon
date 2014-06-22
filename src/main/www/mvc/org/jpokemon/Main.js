Emissary.defineController('org.jpokemon.Main', {
  navItems: '.nav.navbar-nav',
  
  constructor: function() {
    console.log('jpokemon nav bar controller');
    window.JPokemon = window.JPokemon || {};
    JPokemon.NavBar = this;

    $.websocket.init();

    new (Emissary.getController('org.jpokemon.Login'))();
  },

  attachNavItem: function(name, callback, dropdown) {
    var navItem = new (Emissary.getController('org.jpokemon.NavItem'))(name);

    if (callback) {
      navItem.addListener(callback);
    }
    if (dropdown) {
      $.each(dropdown, function(index, row) {
        navItem.addDropdownListener(row.name, row.callback);
      });
    }

    navItem.view.appendTo(this.navItems);
    return navItem;
  }
});
