Emissary.defineController('org.jpokemon.Overworld', {
  players: null,
  nextMove: null,
  nextMoveDelayCounter: 0,
  moveDelayThreshold: 2,

  constructor: function() {
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
    $.websocket.subscribe('overworld-load-map', this.onLoadMap);
    $.websocket.subscribe('overworld-add-player', this.onAddPlayer);
    $.websocket.subscribe('overworld-move', this.onMovePlayer);

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

      this.setupControls();
    }
    else {
      alert("Your browser does not support HTML5 canvas.");
    }
  },

  setupControls: function() {
    // TODO - check the user agent maybe, load controls for a platform
    new (Emissary.getController('org.jpokemon.KeyboardInput'))();
  },

  onUpdate: function(dt) {
    if (JPokemon.input) {
      if (this.nextMove) {
        var direction = 'undefined';

        switch(this.nextMove) {
          case JPokemon.input.keys[0]: direction = 'up'; break;
          case JPokemon.input.keys[1]: direction = 'left'; break;
          case JPokemon.input.keys[2]: direction = 'down'; break;
          case JPokemon.input.keys[3]: direction = 'right'; break;
        }

        if (this.nextMove === JPokemon.input.keyPressed) {
          if (++this.nextMoveDelayCounter >= this.moveDelayThreshold && this.players[JPokemon.player].moveQueue.length < 2) {
            this.nextMoveDelayCounter -= this.moveDelayThreshold;

            $.websocket.send({
              event: 'overworld',
              action: 'move',
              direction: direction
            });
          }
        }
        else if (direction) {
          $.websocket.send({
            event: 'overworld',
            action: 'look',
            direction: direction
          });
          this.nextMove = null;
        }
      }
      else if (JPokemon.input.keyPressed === JPokemon.input.keys[4]) {
        $.websocket.send({
          event: 'overworld',
          action: 'interact'
        });
      }
      else {
        this.nextMove = JPokemon.input.keyPressed;
        this.nextMoveDelayCounter = 0;
      }
    }

    var dirty = false;

    $.each(this.players, function(name, player) {
      dirty = dirty || player.update(dt);
      return !dirty;
    });

    return true;
  },

  onDraw: function(context) {
    $.each(this.players, function(name, player) {
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
    me.levelDirector.loadLevel(mapName);
    me.game.world.addChild(me.pool.pull('pokemon-trainer-draw-layer', entityz));

    $.each(players, function(index, playerJson) {
      this.addPlayer(playerJson);

      if (index < 1) {
        JPokemon.player = playerJson.name;
        this.players[JPokemon.player].entity = me.pool.pull('pokemon-trainer-player', this.players[JPokemon.player].renderable, entityz);
        me.game.world.addChild(this.players[JPokemon.player].entity);
        me.game.viewport.follow(this.players[JPokemon.player].entity, me.game.viewport.AXIS.BOTH);
      }
    }.bind(this));

    me.game.world.sort();
  },

  onAddPlayer: function(json) {
    debugger;
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
    this.players[json.name] = new (Emissary.getController('org.jpokemon.overworld.PokemonTrainer'))(json);
  },

  onMovePlayer: function(json) {
    this.players[json.name].addMoveToQueue(json);
  }
});
