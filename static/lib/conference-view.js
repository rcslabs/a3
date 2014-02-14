
var MediaStream = window.webkitMediaStream || window.MediaStream;

function ConferenceView($el, height) {
  this._$el = $el;
  this._height = height;

  this._localStreams  = {};
  this._remoteStreams = {};
}

ConferenceView.prototype = {
  hasLocalStream : function(stream) {
    return stream.id in this._localStreams;
  },
  hasRemoteStream : function(stream) {
    return stream.id in this._remoteStreams;
  },

  addVideoContainer: function(label, stream) {
    var video = document.createElement('video');
    video.setAttribute('autoplay', 'autoplay');
    video.style.height = this._height - 20 + "px";
    var $container = $('<div></div>').css({textAlign:'center', overflow:'hidden',width:this._height*4/3, height:this._height, border: '1px solid #ccc', position: 'relative', float:'left'})
    $container.append( $('<div></div>').css({width:'100%', overflow:'hidden', fontSize: 12}).text(label) );
    $container.append( video );
    this._$el.append($container);

    attachMediaStream(video, stream);
    return $container;
  },

  addLocalStream: function(stream) {
    if(this.hasLocalStream(stream)) return;
    this._localStreams[stream.id] = this.addVideoContainer(stream.label, stream);
  },
  addRemoteStream: function(stream) {
    if(this.hasRemoteStream(stream)) return;
    this._remoteStreams[stream.id] = this.addVideoContainer(stream.label, stream);
  },
  removeRemoteStream: function(stream) {
    if(!this.hasRemoteStream(stream)) return;

    this._remoteStreams[stream.id].remove()
    this._remoteStreams[stream.id] = null;
    delete this._remoteStreams[stream.id];
  }
}
