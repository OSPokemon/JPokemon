Emissary.defineController('org.jpokemon.overworld.PokemonTrainer', {
  view: false,
  width: 48,
  height: 56,
  tilesize: 32,
  lastMoveTime: null,
  moveSpeed: 500,

  constructor: function(playerJson) {
    this.name = playerJson.name;
    this.font = new me.Font('courier', 12, 'gray');
    this.x = this.lastx = playerJson.x;
    this.y = this.lasty = playerJson.y;
    this.moveSpeed = playerJson.moveSpeed || this.moveSpeed;
    this.moveQueue = [];

    this.renderable = new me.AnimationSheet(0, 0, me.loader.getImage(playerJson.avatar), this.width, this.height, 0);

    this.renderable.addAnimation('walkdown', [0, 1, 2, 1], 100);
    this.renderable.addAnimation('walkleft', [3, 4, 5, 4], 100);
    this.renderable.addAnimation('walkright', [6, 7, 8, 7], 100);
    this.renderable.addAnimation('walkup', [9, 10, 11, 10], 100);

    this.renderable.animationpause = true;
    this.renderable.setCurrentAnimation('walkdown');

    this.renderable.setAnimationFrame(1);
  },

  update: function(dt) {
    if (this.moveQueue.length > 0) {

      var timeNow = new Date().getTime(),
          percentageMoved = (timeNow - this.lastMoveTime) / this.moveSpeed;

      if (!this.lastMoveTime) {
        // free move
        this.lastMoveTime = new Date().getTime();
        percentageMoved = 0;
      }

      if (percentageMoved > 1) {
        this.x = this.lastx = this.moveQueue[0].x;
        this.y = this.lasty = this.moveQueue[0].y;
        this.renderable.animationpause = true;
        this.renderable.setAnimationFrame(1);
        this.moveQueue.shift();
        this.lastMoveTime = false;
      }
      else {
        if (this.x === this.lastx && this.y === this.lasty) {
          this.renderable.setCurrentAnimation(this.moveQueue[0].animation);
          this.renderable.animationpause = false;
        }

        // The subtraction accounts for the direction
        this.x = this.lastx + ((this.moveQueue[0].x - this.lastx) * percentageMoved);
        this.y = this.lasty + ((this.moveQueue[0].y - this.lasty) * percentageMoved);
      }
    }

    return this.renderable.update(dt);
  },

  getScreenX: function() {
    return this.x * this.tilesize - ((this.width - this.tilesize) / 2); // center the dude in the tile
  },

  getScreenY: function() {
    return this.y * this.tilesize - (this.height - this.tilesize); // dude at bottom of tile
  },

  draw: function(context) {
    var nameWidth = this.font.measureText(context, this.name).width, // + namePadding * 2;
        nameLeft = this.x * this.tilesize + (this.tilesize / 2) - (nameWidth / 2),
        nameTop = this.y * this.tilesize - (this.height - this.tilesize);

    context.save();

    context.translate(this.getScreenX(), this.getScreenY());
    this.renderable.draw(context);

    context.translate(-this.getScreenX(), -this.getScreenY());

    context.globalAlpha = 0.75;
    context.fillStyle = 'black';
    context.fillRect(nameLeft, nameTop, nameWidth, 14);
    context.globalAlpha = 1.0;
    this.font.draw(context, this.name, nameLeft, nameTop);

    context.restore();
  },

  addMoveToQueue: function(moveConfig) {
    console.log('player: [' + this.name + '] push move queue: ' + JSON.stringify(moveConfig));
    this.moveQueue.push(moveConfig);
  }
});
