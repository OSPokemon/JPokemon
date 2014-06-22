(function($) {
  // Courtesy of StackOverflow!
  // http://stackoverflow.com/questions/210717/using-jquery-to-center-a-div-on-the-screen
  $.fn.center = function () {
    this.css("position","absolute");
    this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) + $(window).scrollTop()) + "px");
    this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) + $(window).scrollLeft()) + "px");
    return this;
  }
})(jQuery);

(function($) {
  var websocket,
      listeners = {};

  $.extend({
    websocket: {
      init: function(url) {
        url = url || 'ws://'+document.location.hostname + ':' + document.location.port + '/ws';
        websocket = new WebSocket(url);
        websocket.onmessage = $.websocket.dispatch;
      },
      dispatch: function(message) {
        var json = JSON.parse(message.data),
            event = json.event,
            callbacks = listeners[event];

        if (callbacks) {
          $.each(callbacks, function(index, callback) {
            callback(json);
          })
        }
      },
      send: function(json) {
        websocket.send(JSON.stringify(json));
      },
      subscribe: function(event, callback) {
        listeners[event] = listeners[event] || [];
        return listeners[event].push(callback) - 1;
      },
      unsubscribe: function(event, callbackId) {
        if (listeners[event]) {
          listeners[event][callbackId] = null;
        }
      }
    }
  });
})(jQuery);
