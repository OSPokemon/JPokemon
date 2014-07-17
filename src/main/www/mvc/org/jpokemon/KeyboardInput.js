Emissary.defineController('org.jpokemon.KeyboardInput', {
  view: false,
  canvasIsFocused: false,
  keyPressed: null,

  constructor: function() {
    this.keys = [87, 65, 83, 68, 69];
    this.canvas = $('#screen')[0].children[0];
    this.listeners = {};

    $(document).mouseup(this.onClick);
    $(document).keydown(this.onKeydown);
    $(document).keyup(this.onKeyup);

    window.JPokemon = window.JPokemon || {};
    JPokemon.input = this;
  },

  onClick: function(e) {
    if (this.canvas == e.target) {
      this.canvasIsFocused = true;
    }
    else {
      this.canvasIsFocused = false;
    }
  },

  onKeydown: function(e) {
    if (this.keyPressed || !this.canvasIsFocused || this.keys.indexOf(e.keyCode) < 0) {
      return;
    }

    this.keyPressed = e.keyCode;
  },

  onKeyup: function(e) {
    var keyCode = e.keyCode;

    if (keyCode !== this.keyPressed || !this.canvasIsFocused) {
      return;
    }

    this.keyPressed = null;
  }
});
