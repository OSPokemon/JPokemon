Emissary.defineController('org.jpokemon.Login', {
  formElement: '.formElement',
  usernameInputField: '.username-input',
  passwordInputField: '.password-input',
  loginButton : '.login-button',
  signupButton: '.signup-button',
  loginSuccessCallbackId: -1,
  loginErrorCallbackId: -1,
  navItem: null,

  constructor: function() {
    console.log('login controller');

    this.view.center();
    this.view.draggable();

    this.formElement.submit(this.onFormSubmit.bind(this));

    this.navItem = JPokemon.NavBar.attachNavItem('Login', this.toggleVisibility.bind(this));
    this.toggleVisibility(this.navItem);
  },

  toggleVisibility: function(navItem) {
    navItem.view.toggleClass('active');
    this.view.toggle();
  },

  onFormSubmit: function(e) {
    e.preventDefault();

    var username = this.usernameInputField.val(),
        password = this.passwordInputField.val();

    this.loginSuccessCallbackId = $.websocket.subscribe('login', this.onLoginSuccess.bind(this));
    this.loginErrorCallbackId = $.websocket.subscribe('loginError', this.onLoginError.bind(this));

    $.websocket.send({
      username: username,
      password: password
    });
  },

  onLoginSuccess: function(json) {
    $.websocket.unsubscribe('login', this.loginSuccessCallbackId);
    $.websocket.unsubscribe('loginError', this.loginErrorCallbackId);

    this.formElement.addClass('has-success');

    setTimeout(function() {
      this.view.toggle({
        effect: 'blind',
        duration: 800,
        complete: function() {
          this.navItem.view.remove();
          this.view.remove();
        }.bind(this)
      });
    }.bind(this), 500);
  },

  onLoginError: function(json) {
    // TODO
    console.log(json);
  }
});
