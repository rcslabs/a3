



var MainView = Backbone.View.extend({

  initialize: function() {
    this.listenTo(this.model, "change:state", this.stateChanged);
    this.listenTo(this.model, "change:tab",   this.tabChanged);
  },

  //template : new EJS({url: '/...../template.ejs'})
    //html = new EJS({url: 'tmpl/login.ejs'}).render(this.model);
     //this.$el.html(html);

  stateChanged : function() {
    var state = this.model.get("state");

    if(state === "login") {
      this.$el.addClass('show-login');
      try {  $('#login-phone')   .val(window.localStorage.getItem('saved-login-phone'));     }catch(e) {}                  // saved phone-password
      try {  $('#login-password').val(window.localStorage.getItem('saved-login-password'));  }catch(e) {}
    } else if(state === "main") {
      this.$el.removeClass('show-login');
    }
  },


  tabChanged : function() {
    var tab = this.model.get("tab");

    this.$el.removeClass('show-contacts-tab').removeClass('show-dial-tab').removeClass('show-call-log-tab');

    switch(tab) {
      case "contacts":
        this.$el.addClass('show-contacts-tab');
        break;
      case "dial":
        this.$el.addClass('show-dial-tab')
        break;
      case "call-log":
        this.$el.addClass('show-call-log-tab');
        break;
    }
  }
});


var CallView = Backbone.View.extend({
  initialize: function() {
    this.render();
    this.listenTo(this.model, "change:state",       this.stateChanged);
    this.listenTo(this.model, "change:videoStream", this.videoStreamChanged);
  },
  events: {
    "click .hangup": "hangup"
  },
  template : new EJS({url: 'tmpl/call.ejs'}),
  hangup: function() {
    /// ........ bad style
    var fn = this.model.get("hangup");
    if(typeof fn === "function") {
      fn();
    }
  },
  render: function() {
    var html = this.template.render(this.model);
    this.$el.html(html);
  },
  stateChanged: function() {
    var state = this.model.get("state");
    switch(state){
      case "CALL_STARTING":
        this.$el.attr("data-state", "starting");
        break;
      case "CALL_IN_PROGRESS":
        this.$el.attr("data-state", "in-progress");
        break;
      case "CALL_FINISHED":
        this.$el.attr("data-state", "finished");
        break;
      case "CALL_REMOVED":
        this.$el.attr("data-state", "");
        break;
    }
  },
  videoStreamChanged: function() {
    console.log("Attaching video stream");
    var videoStream = this.model.get('videoStream');
    var $video = this.$el.find('video');
    var $audio = this.$el.find('audio');
    if ($video.length) {
      attachMediaStream($video[0], videoStream);
    } else {
      attachMediaStream($audio[0], videoStream);
    }
  }
});



var CallsListView = Backbone.View.extend({
  initialize: function() {
    this.listenTo(this.model, "add",    this.added);
    this.listenTo(this.model, "remove", this.removed);
  },
  events : {
  },
  render: function() {
    //
  },
  added: function(callModel) {
    var callView = new CallView({model: callModel, el: $('<li></li>').appendTo(this.$el)})
  },
  removed: function() {
    
  }
});








//////////////////////
jQuery(function($){
  
  // toggle groups
  $('.group-name').click(function(e){
    $('.group-name').removeClass('opened');
    $(this).addClass('opened');
  });

  $('.contact').click(function(e){
    $('.contact').removeClass('focused');
    $(this).addClass('focused')
  });

  $('.settings-button').click(function(){
    $('#multifon-content').toggleClass('show-settings');
  });
});


