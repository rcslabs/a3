

var UserState = Backbone.Model.extend({

  defaults: {
    state   : "",
    tab     : ""
  },

  initialize: function() {
    //
  }
});



var CallModel = Backbone.Model.extend({
  defaults: {
    bUri    : null,
    state   : "",
    hangup  : null,
    audio   : true,
    video   : false,
    videoStream : null
  },

  initialize: function() {
  },

  addRemoteVideoStream: function(stream) {
    this.set('videoStream', stream);
  },
  removeVideoStream: function(stream)  {
    this.set('videoStream', null);
  }

});


var CallsCollection = Backbone.Collection.extend({
  model: CallModel,

  initialize: function() {
    this.bind( 'add', this.onCallAddedd, this );
  },
  onCallAddedd: function(x) {
    //
  },

  addRemoteVideoStream: function(stream) {
    console.log("Model:add stream");
    this.forEach(function(call) {call.addRemoteVideoStream(stream)});
  },

  removeRemoteVideoStream: function(stream) {
    console.log("Model:add stream")
    this.forEach(function(call) {call.removeRemoteVideoStream(stream)});
  }
});
