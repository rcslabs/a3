// 
// adapter.js
// 
var RTCPeerConnection = null;
var getUserMedia = null;
var attachMediaStream = null;

if (navigator.mozGetUserMedia) {
  console.log("This appears to be Firefox");

  // The RTCPeerConnection object.
  RTCPeerConnection = mozRTCPeerConnection;

  // Get UserMedia (only difference is the prefix).
  // Code from Adam Barth.
  getUserMedia = navigator.mozGetUserMedia.bind(navigator);

  // Attach a media stream to an element.
  attachMediaStream = function(element, stream) {
    console.log("Attaching media stream");
    element.mozSrcObject = stream;
    element.play();
  };
} else if (navigator.webkitGetUserMedia) {
  console.log("This appears to be Chrome");

  // The RTCPeerConnection object.
  RTCPeerConnection = webkitRTCPeerConnection;
  
  // Get UserMedia (only difference is the prefix).
  // Code from Adam Barth.
  getUserMedia = navigator.webkitGetUserMedia.bind(navigator);

  // Attach a media stream to an element.
  attachMediaStream = function(element, stream) {
    var url = webkitURL.createObjectURL(stream);
    console.log("AttachMediaStream ", url)
    element.src = url;
  };
} else {
  console.log("Browser does not appear to be WebRTC-capable");
}




//
//
//
function WebrtcMedia(listener, vv) {
  this.listener = listener;
  this.vv = vv && vv.length == 2 ? vv : [true, true];

  //this.container = document.createElement('div');
  //this.container.style.position = 'absolute';
  //this.container.style.left = '0px';
  //this.container.style.top = '0px';
  //this.container.style.width = '400px';
  //this.container.style.height = '400px';
  //this.container.style.border = '1px solid black';

  //this.videoSubscribe = document.createElement('video');
  //this.videoSubscribe.style.width = '100%';
  //this.videoSubscribe.style.height = '100%';
  //this.videoSubscribe.setAttribute('autoplay', 'autoplay');
  //this.container.appendChild(this.videoSubscribe);

  this.subscribeStream = null;

  this.videoPublish = document.createElement('video');
  //this.videoPublish.style.position = 'absolute';
  //this.videoPublish.style.right = '5%';
  //this.videoPublish.style.bottom = '5%';
  //this.videoPublish.style.width = '40%';
  //this.videoPublish.style.height = '40%';
  //this.videoPublish.style.border = '1px solid black';
  //this.videoPublish.setAttribute('autoplay', 'autoplay');
  //this.container.appendChild(this.videoPublish);

  this.publishStream = null;
  //document.body.appendChild(this.container);


  this.pc = null;


  window.media = this;


  // HACK((((
  var self = this;
  window.setTimeout(function() {
    self.listener.swfReady("ru.rcslabs.webcall.MediaTransport");
  }, 50);
}
WebrtcMedia.prototype = {


  showSettingsPanel : function (centerPanel, index)                       { throw "Not implemented"; },
  hideSettingsPanel : function ()                                         { throw "Not implemented"; },
  loadImage         : function (url)                                      { throw "Not implemented"; },
  setViewState      : function (state)                                    { throw "Not implemented"; },
  publish           : function (voicePublisherName, videoPublisherName)   { throw "Not implemented"; },
  unpublish         : function ()                                         { throw "Not implemented"; },
  subscribe         : function (voiceSubscriberName, videoSubscriberName) { throw "Not implemented"; },
  unsubscribe       : function ()                                         { throw "Not implemented"; },
  muteMicrophone    : function (value)                                    { throw "Not implemented"; },
  microphoneVolume  : function (value)                                    { throw "Not implemented"; },
  muteSound         : function (value)                                    { throw "Not implemented"; },
  soundVolume       : function (value)                                    { throw "Not implemented"; },



  getVersion : function () { return "0.0.1"; },

  getHardwareState : function (){
    var self = this;
    try  {
      navigator.webkitGetUserMedia({
        audio: !!self.vv[0],
        video: !!self.vv[1] && {
          mandatory: { maxWidth: 320, maxHeight: 240, maxFrameRate: 15 }
        },
      }, function (stream) {
        console.log("local media allowed");
        return self.onGotPublishStream(stream);
      }, function () {
        alert("navigator.webkitGetUserMedia() error!");
        self.listener.mediaHandler("HardwareEvent.HARDWARE_STATE", {
          camera: {
            state: 'disabled'
          },
          microphone: {
            state: 'disabled'
          }
        });
      });
      console.log("Requested access to local media.");
    } catch (e) {
      alert("webkitGetUserMedia() failed.");
      console.log("webkitGetUserMedia failed with exception: " + e.message);
    }
  },



  // public
  
  addCandidate : function(msg) {
    var self = this;
    window.setTimeout(function() {
      //debugger;
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:1043806978 1 udp 2113937151 192.168.0.10 62944 typ host generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:1043806978 1 udp 1677729535 89.67.140.156 62944 typ srflx generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:1043806978 2 udp 2113937151 192.168.0.10 62944 typ host generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:1043806978 2 udp 1677729535 89.67.140.156 62944 typ srflx generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:4233069003 1 tcp 1509957375 192.168.56.1 63325 typ host generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:4233069003 2 tcp 1509957375 192.168.56.1 63325 typ host generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:469649836 1 tcp 1509957375 192.168.0.10 63326 typ host generation 0\r\n"}
      //{"sdpMLineIndex":0, "sdpMid":"audio", "candidate":"a=candidate:469649836 2 tcp 1509957375 192.168.0.10 63326 typ host generation 0\r\n"}

      var pc = self.getPeerConnection();

      var candidate = new RTCIceCandidate({
        sdpMLineIndex : 0, //msg.label,
        sdpMid        : "audio",
        candidate     : msg.candidate + "\r\n"
      });
      console.log("Adding candidate ", candidate);

      pc.addIceCandidate(candidate);
    }, 0);
  },



  setOffererSDP : function(message) {
    var self = this;
    var pc = this.getPeerConnection();
    var mediaConstraints = {
      'mandatory': {
        'OfferToReceiveAudio': true,
        'OfferToReceiveVideo': true
      }
    };

    var offerSDP = message.sdp;

    if(self.isOfferSDPSet) {
      //console.log("OFFER SDP IS ALREADY SET");
      //return ;
    }

    // A block to postpone
    // offer SDP on 15 seconds
    //
    //if(offerSDP.indexOf("a=ssrc:") != -1 && offerSDP.indexOf("m=video") != -1) {
    //  self.kf();
    //
    //  window.setTimeout(function() {
    //    console.log("SET POSTPONED SDP " + offerSDP);
    //    self.offerSDP  = offerSDP;
    //    var remoteDescription = window.remoteDescription = new RTCSessionDescription({sdp: offerSDP, type: 'offer'});
    //    pc.setRemoteDescription(remoteDescription);
    //
    //    console.log("pc.CreatingAnswer", pc);
    //    pc.createAnswer(function(localDescription) {
    //      console.log("Answerer SDP=", localDescription.sdp);
    //      pc.setLocalDescription(localDescription);
    //      message.sdp = localDescription.sdp;
    //      self.listener.mediaHandler("media:local-sdp", message);
    //    }, null, mediaConstraints);
    //  }, 15000);
    //  return;
    //}


   this.offerSDP  = offerSDP;

    //{
    //  var l = offerSDP.split("\r\n");
    //  var m = "";
    //  for(var i = 0; i < l.length; i++) {
    //    line = l[i];
    //    if(line.match(/^m=(\S+)/)) m = RegExp.$1;
    //    if(line.match(/^a=candidate/))
    //      this.addCandidate({label: m, candidate: line})
    //  }
    //}

    console.log("setOffererSDP:", offerSDP);

    // ???? Move to somewhere
    var mediaConstraints = {
      'mandatory': {
        'OfferToReceiveAudio': true,
        'OfferToReceiveVideo': true
      }
    };
    pc.addStream(this.publishStream);


    var sessionDescription = window.remoteDescription = new RTCSessionDescription({sdp: offerSDP, type: 'offer'});
    pc.setRemoteDescription(sessionDescription);
    console.log("pc.CreatingAnswer", pc);
    pc.createAnswer(function(sessionDescription) {
      console.log("Answerer SDP=", sessionDescription.sdp);
      //sessionDescription.sdp = preferOpus(sessionDescription.sdp);
      pc.setLocalDescription(sessionDescription);
      message.sdp = sessionDescription.sdp;
      self.listener.mediaHandler("media:local-sdp", message);

      self._startKfHack();
    }, null, mediaConstraints);
  },


  kf: function() {
    var pc = this.getPeerConnection();
    console.log("KeyFrame magic");
    pc.setRemoteDescription(pc.remoteDescription,function(){
      pc.setLocalDescription(pc.localDescription,function(){});
    });
  },

  _startKfHack  : function() {
    if(!this._kfHack) {
      var self = this;
      //this._kfHack = window.setTimeout(function() { self.kf() }, 4000);
    }
  },
  _stopKfHack : function() {
    if (this._kfHack) {
      window.clearInterval(this._kfHack);
      this._kfHack = null;
    }
  },

  // private
  
  
  getPeerConnection : function() {
    
    var self = this;

    if(this.pc)
      return this.pc;

    //servers = {"iceServers":[
    //            {"url":"stun:<stun_server>:<port>},
    //            {"url": "stun:stun.l.google.com:19302"}
    //            { url: "turn:<user>@<turn_server>:<port>",credential:"<password>"}
    //        ]};

    var pc_config = {"iceServers": []};

    var pc_constraints = {"optional": [{"DtlsSrtpKeyAgreement": false}]};

    var pc = new RTCPeerConnection(pc_config, pc_constraints);
    pc.onicecandidate = function(event) {
      //debugger;
      if (event.candidate) {
        console.log('onicecandidate', {type: 'candidate',
                   label: event.candidate.sdpMLineIndex,
                   id: event.candidate.sdpMid,
                   candidate: event.candidate.candidate});
      } else {
        console.log("End of candidates.");
      }
    };
    pc.onconnecting = function(event) {
      console.log("Session connecting",  event);
    }
    pc.onopen = function(event) {
      console.log("Session opened.",  event);
    }
    pc.onaddstream = function(event) {
      self.listener.mediaHandler("media:add-remote-stream", event.stream);

      console.log("Remote stream added.", event);

      self.subscribeStream = event.stream;


      //attachMediaStream(self.videoSubscribe, self.subscribeStream);
      //waitForRemoteVideo();
      //if (remoteStream.videoTracks.length === 0 || remoteVideo.currentTime > 0) {
      //  transitionToActive();
      //  } else {
      //  setTimeout(waitForRemoteVideo, 100);
      //}

      //window.setTimeout(function() {
      //  console.log("#subscribeStream.=", self.subscribeStream);
      //}, 1000);
    }
    pc.onremovestream = function(event) {
      console.log("Remote stream removed.", event);
      self.listener.mediaHandler("media:remove-remote-stream", event.stream);

    }
    // DEBUG!
    window.pc = pc;
    return (this.pc = pc);
  },



  onGotPublishStream : function (stream) {
    var self = this;
    self.listener.mediaHandler("media:add-local-stream", stream);

    console.log("navigator.webkitGetUserMedia() success");
    this.publishStream = stream;
    //attachMediaStream(this.videoPublish, stream);

    this.listener.mediaHandler("HardwareEvent.HARDWARE_STATE", {
      camera: {
        state: 'enabled'
      },
      microphone: {
        state: 'enabled'
      }
    });
  },

  close : function() {
    if(this.pc) {
      console.log("closing media");
      try {
        this.pc.close();
      } catch(err) {
        console.log("Error in close", err)
      }
      this.pc = null;
    }
    this._stopKfHack();
  }
}

