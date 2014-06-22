Emissary.defineController('org.jpokemon.NavItem', {
  name: 'a',
  dropdown: null,
  listeners: null,
  
  constructor: function(name) {
    if (name) {
      this.setName(name);
    }

    this.name.click(this.onClick.bind(this));
  },

  onClick: function(e) {
    if (this.listeners) {
      $.each(this.listeners, function(index, callback) {
        callback(this, e);
      }.bind(this));
    }
  },

  setName: function(name) {
    if (this.dropdown) {
      this.name.html(name + ' <b class="caret"></b>');
    }
    else {
      this.name.html(name);
    }
  },

  addListener: function(callback) {
    if (!this.listeners) {
      this.listeners = [];
    }
    this.listeners.push(callback);
  },
  
  addDropdownListener: function(name, callback) {
    if (!this.dropdown) {
      this.dropdown = $('<ul class="dropdown-menu"></ul>');
      this.dropdown.appendTo(this.view);
      this.name.addClass('dropdown-toggle');
      this.name.attr("data-toggle", "dropdown");
      this.name.html(this.name.html() + ' <b class="caret"></b>');
    }

    var navItem = new (Emissary.getController('org.jpokemon.NavItem'))();
    navItem.setName(name);
    navItem.addListener(callback);
    navItem.view.appendTo(this.dropdown);
  }
});
