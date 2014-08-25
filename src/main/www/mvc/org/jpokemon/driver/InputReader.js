Emissary.defineController('org.jpokemon.driver.InputReader', {
  view: false,
  oldInputValue: null,
  oldInputTime: null,
  shortTimeout: 50,
  longTimeout: 250,

  constructor: function() {
    this.onCallback();
  },

  onCallback: function() {
    if (!JPokemon.input) {
      window.setTimeout(this.onCallback, 3000);
      return;
    }

    if (!JPokemon.input.value) {
      // poll like mad until we see somethin
      window.setTimeout(this.onCallback, this.shortTimeout);
    }
    // we saw somethin then
    else {
      window.setTimeout(this.onCallback, this.longTimeout);
    }

    if (JPokemon.input.value === 'interact') {
      $.websocket.send({
        event: 'overworld',
        action: 'interact'
      });
    }
    // If ever we poll and the input is different, we send a look
    else if (JPokemon.input.value !== this.oldInputValue) {
      // unless they don't have a look direction
      if (this.oldInputValue) {
        $.websocket.send({
          event: 'overworld',
          action: 'look',
          direction: this.oldInputValue
        });
      }

      // record our most recent results
      // if it changed since measuring, use the new value anyway
      this.oldInputValue = JPokemon.input.value;
    }
    // they have to be equal at this point
    else if (this.oldInputValue) {
      // now they both have to exist and be the same
      $.websocket.send({
        event: 'overworld',
        action: 'move',
        direction: this.oldInputValue
      });
    }
  }
});
