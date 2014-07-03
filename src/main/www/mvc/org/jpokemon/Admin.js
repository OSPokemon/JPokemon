Emissary.defineController('org.jpokemon.Admin', {
  adminControlList: '.list-group',
  cardLayout: '.col-md-9',
  adminControls: null,
  authorizationHeader: null,

  constructor: function(authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
    this.adminControls = {};

    $.getCss(Emissary.viewRoot + 'org/jpokemon/Admin.css');

    JPokemon.NavBar.attachNavItem('Admin Tools', this.toggleVisibility.bind(this));

    this.attachAdminController('Abilities', 'org.jpokemon.admin.Ability');
    this.attachAdminController('Contest Categories', 'org.jpokemon.admin.ContestCategory');
  },

  toggleVisibility: function(navItem) {
    navItem.view.toggleClass('active');
    this.view.toggle();
  },

  attachAdminController: function(name, controller) {
    var controller = new (Emissary.getController(controller))(this.authorizationHeader),
        listLink = $('<a href="#" class="list-group-item">' + name + '</a>');

    controller.view.remove();
    this.adminControls[name] = controller;
    listLink.click(this.onClickControl(name));
    listLink.appendTo(this.adminControlList);
  },

  onClickControl: function(name) {
    return function(e) {
      $('a', this.adminControlList).removeClass('active');
      $(e.target).addClass('active');
      this.cardLayout.empty();
      this.adminControls[name].view.appendTo(this.cardLayout);
      this.adminControls[name].update();
    }.bind(this);
  }
});
