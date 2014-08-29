Emissary.defineController('org.jpokemon.overworld.Overworld', {
  constructor: function() {

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
    $.websocket.subscribe('overworld-load-map', this.onLoadMap);
    $.websocket.subscribe('overworld-add-player', this.onAddPlayer);
    $.websocket.subscribe('overworld-remove-player', this.onRemovePlayer);
    $.websocket.subscribe('overworld-move', this.onMovePlayer);
    $.websocket.subscribe('overworld-look', this.onLookPlayer);

    // (ab)use melonjs to treat this as a drawable screen entity
    me.pool.register('pokemon-trainer-draw-layer', me.ObjectEntity.extend({
      init: function(z) {
        this.parent(0, 0, {
          width: 1,
          height: 1
        });
        this.name = 'pokemon-trainer-draw-layer';
        this.z = z;
      },
      update: this.onUpdate,
      draw: this.onDraw
    }), false);

    me.pool.register('pokemon-trainer-player', me.ObjectEntity.extend({
      init: function(renderable, entityz) {
        this.renderable = renderable;
        this.z = entityz;
      },
      update: function() { },
      draw: function(context) { /* let draw layer draw us */ }
    }), true);
  },

  onLogin: function() {
    if (me.video.init("screen", 640, 480, true, 'auto')) {
      me.audio.init("mp3,ogg");
      me.state.change(me.state.PLAY);

      JPokemon.canvas = $('#screen')[0].children[0];
    }
    else {
      alert("Your browser does not support HTML5 canvas.");
    }
  },

  onUpdate: function(dt) {
    $.each(JPokemon.players, function(name, player) {
      player.update(dt);
    });

    return true;
  },

  onDraw: function(context) {
    $.each(JPokemon.players, function(name, player) {
      player.draw(context);
    });
  },

  onLoadMap: function(json) {
    var mapName = json.mapName,
        players = json.players,
        entityz = json.entityz,
        requests = [];

    if (!me.loader.getTMX(mapName)) {
      requests.push({
        name: mapName,
        type: 'tmx',
        src: 'map/' + mapName + '.tmx'
      });
    }
    $.each(json.tilesets, function(i, tileset) {
      if (!me.loader.getImage(tileset)) {
        requests.push({
          name: tileset,
          type: 'image',
          src: 'image/tileset/' + tileset + '.png'
        });
      }
    });
    $.each(players, function(name, player) {
      if (!me.loader.getImage(player.avatar)) {
        requests.push({
          name: player.avatar,
          type: 'image',
          src: 'image/avatar/' + player.avatar + '.png'
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
            this.loadMap(mapName, entityz, players);
          }
        }.bind(this));
      }.bind(this));
    }
    else {
      this.loadMap(mapName, entityz, players);
    }
  },

  loadMap: function(mapName, entityz, players) {
    JPokemon.players = {};
    me.levelDirector.loadLevel(mapName);
    me.game.world.addChild(me.pool.pull('pokemon-trainer-draw-layer', entityz));

    $.each(players, function(index, playerJson) {
      this.addPlayer(playerJson);

      if (index < 1) {
        JPokemon.player = playerJson.name;
        JPokemon.players[JPokemon.player].entity = me.pool.pull('pokemon-trainer-player', JPokemon.players[JPokemon.player].renderable, entityz);
        me.game.world.addChild(JPokemon.players[JPokemon.player].entity);
        me.game.viewport.follow(JPokemon.players[JPokemon.player].entity, me.game.viewport.AXIS.BOTH);
      }
    }.bind(this));

    me.game.world.sort();
  },

  onAddPlayer: function(json) {
    if (!me.loader.getImage(json.avatar)) {
      me.loader.load({
        name: json.avatar,
        type: 'image',
        src: 'image/avatar/' + json.avatar + '.png'
      }, function() {
        this.addPlayer(json);
      }.bind(this));
    }
    else {
      this.addPlayer(json);
    }
  },

  addPlayer: function(json) {
    JPokemon.players[json.name] = new (Emissary.getController('org.jpokemon.overworld.PokemonTrainer'))(json);
  },

  onMovePlayer: function(json) {
    JPokemon.players[json.name].addMoveToQueue(json);
  },

  onLookPlayer: function(json) {
    JPokemon.players[json.name].renderable.setCurrentAnimation('walk' + json.direction);
    JPokemon.players[json.name].renderable.animationpause = true;
  },

  onRemovePlayer: function(json) {
    delete JPokemon.players[json.name];
  }
});
