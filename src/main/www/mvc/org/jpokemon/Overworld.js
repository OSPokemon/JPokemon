Emissary.defineController('org.jpokemon.Overworld', {
  players: null,

  constructor: function() {
    console.log('new overworld constructor');
    this.players = {};

    me.sys.fps = 30;
    me.sys.pauseOnBlur = false;

    // (ab)use melonjs to turn off screen handling
    me.state.set(me.state.PLAY, new (me.ScreenObject.extend({
      init : function(config) {
        this.parent(config);
      },

      onResetEvent: function(config) {
        console.log('onResetEvent');
      },
      
      onDestroyEvent: function() {
        this.parent();
      }
    })));

    $.websocket.subscribe('login', this.onLogin);
    $.websocket.subscribe('overworld-load-map', this.loadMap);
    $.websocket.subscribe('overworld-add-player', this.addPlayer);

    // (ab)use melonjs to treat this as a drawable screen entity
    me.pool.register('pokemon-trainer-draw-layer', me.ObjectEntity.extend({
      init: function() {
        console.log('new pokemon trainer draw layer');
      },
      update: this.onUpdate,
      draw: this.onDraw
    }), false);

    me.pool.register('pokemon-trainer-player', me.ObjectEntity.extend({
      init: function() {},
      update: function() {
        console.log('update trainer player');
      },
      draw: function(context) { this.parent(context) }
    }), true);
  },

  onLogin: function() {
    if (me.video.init("screen", 640, 480, true, 'auto')) {
      me.audio.init("mp3,ogg");
      me.state.change(me.state.PLAY);
    }
    else {
      alert("Your browser does not support HTML5 canvas.");
    }
  },

  onUpdate: function() {
    console.log('melon js update loop');
  },

  onDraw: function(context) {
    console.log('melon js draw loop');
  },

  loadMap: function(json) {
    var mapName = json.mapName,
        players = json.players,
        tilesets = json.tilesets,
        requests = [];

    if (!me.loader.getTMX(mapName)) {
      requests.push({
        name: mapName,
        type: 'tmx',
        src: 'map/' + mapName + '.tmx'
      });
    }
    $.each(tilesets, function(i, tileset) {
      if (!me.loader.getImage(tileset)) {
        requests.push({
          name: tileset,
          type: 'image',
          src: 'image/tileset/' + tileset + '.png'
        });
      }
    });

    if (requests.length > 0) {
      $.each(requests, function(i, request) {
        me.loader.load(request, function() {
          requests = $.grep(requests, function(value) {
            return value.name != request.name;
          });

          if (requests.length == 0) {
            this._loadMap(mapName, players);
          }
        }.bind(this));
      }.bind(this));
    }
    else {
      this._loadMap(mapName, players);
    }
  },

  _loadMap: function(mapName, players) {
    me.levelDirector.loadLevel(mapName);
    me.game.world.addChild(me.pool.pull('pokemon-trainer-draw-layer'));

    $.each(players, function(index, playerJson) {
      this.addPlayer(playerJson);

      if (index < 1) {
        this.players[playerJson.name].entity = me.pool.pull('pokemon-trainer-player', playerJson);
        me.game.world.addChild(this.players[playerJson.name].entity);
        me.game.viewport.follow(this.players[playerJson.name].entity, me.game.viewport.AXIS.BOTH);
      }
    }.bind(this));
  },

  addPlayer: function(json) {
    this.players[json.name] = new (Emissary.getController('org.jpokemon.overworld.PokemonTrainer'))(json);
  }
});
