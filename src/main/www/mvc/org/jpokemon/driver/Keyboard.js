Emissary.defineController('org.jpokemon.driver.Keyboard', {
  view: false,
  canvasIsFocused: false,

  constructor: function() {
    this.setKeys({
      87: 'up',
      65: 'left',
      83: 'down',
      68: 'right',
      69: 'interact'
    });

    $(document).mouseup(this.onClick);
    $(document).keydown(this.onKeydown);
    $(document).keyup(this.onKeyup);

    window.JPokemon = window.JPokemon || {};
    JPokemon.input = this;
  },

  setKeys: function(keys) {
    this.keys = {};
    for (var key in keys) {
      this.keys[key] = keys[key];
      // set up backwards links, just because
      this.keys[keys[key]] = key;
    }
  },

  onClick: function(e) {
    if (JPokemon.canvas == e.target) {
      this.canvasIsFocused = true;
    }
    else {
      this.canvasIsFocused = false;
    }
  },

  onKeydown: function(e) {
    var keyCode = e.keyCode;

    if (this.value || !this.canvasIsFocused || !this.keys[keyCode]) {
      return;
    }

    this.value = this.keys[e.keyCode];
  },

  onKeyup: function(e) {
    var keyCode = e.keyCode;

    if (!this.value || !this.canvasIsFocused || !this.keys[keyCode]) {
      return;
    }

    if (this.keys[keyCode] === this.value) {
      this.value = null;
    }
  }
});
