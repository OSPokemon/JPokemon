(function(Emissary) {
  var controllers = {},
      views = {};

  Emissary.controllerRoot = 'js/';
  Emissary.viewRoot = 'html/';

  Emissary.getController = function(name) {
    return controllers[name] || Emissary.loadController(name);
  };

  Emissary.loadController = function(name) {
    var url = Emissary.controllerRoot + name.replace(/\./g, '/') + '.js';

    $.ajax({
      url : url,
      dataType: 'script',
      async: false,
      error: function(jqXhr, textStatus, error) {
        console.log(error);
      }
    });

    return controllers[name];
  };

  Emissary.defineController = function(name, config) {
    var constructor = function Controller() {
      this.class = name;

      if (config.hasOwnProperty('view')) {
        var viewTemplate = config.view,
            viewSelection = $(viewTemplate);

        if (viewSelection.length > 0) {
          this.view = viewSelection;
        }
        else if (viewTemplate) {
          this.view = $(Emissary.getView(viewTemplate)).appendTo('body');
        }
      }
      else {
        this.view = $(Emissary.getView(name)).appendTo('body');
      }

      for (var key in config) {
        if (key === 'view') continue;
        var value = this[key] = config[key];

        if ($.type(value) === 'string') {
          var selection = $(value, this.view);
          if (selection.length > 0) this[key] = selection;
        }
        else if ($.type(value) === 'object') {
          this[key] = new (Emissary.getController(value.controller))();
          if (value.selector && this[key].view) $(value.selector, this.view).replaceWith(this[key].view);
        }
      }

      if (config.hasOwnProperty('constructor')) config.constructor.apply(this, arguments);
    };

    for (var fn in config) {
      if (typeof config[fn] === 'function' && fn !== 'constructor') {
        constructor.prototype[fn] = config[fn];
      }
    }

    controllers[name] = constructor;
  };

  Emissary.getView = function(name) {
    return views[name] || Emissary.loadView(name);
  };

  Emissary.loadView = function(name) {
    var url = Emissary.viewRoot + name.replace(/\./g, '/') + '.html';

    $.ajax({
      url : url,
      async : false,
      success : function(data) {
        views[name] = data;
      },
      error: function(jqXhr, textStatus, error) {
        console.log(error);
      }
    });

    return views[name];
  };

})(window.Emissary = window.Emissary || {});
