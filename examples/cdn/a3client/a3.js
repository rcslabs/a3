var a3;
(function (a3) {
    if (typeof LOG === "undefined")
        LOG = function () {
        };
    if (typeof ERROR === "undefined")
        ERROR = function () {
        };

    var MIN_FLASH_VERSION = '10.3.0';

    var SessionEvent = (function () {
        function SessionEvent() {
        }
        SessionEvent.SESSION_STARTED = "SESSION_STARTED";
        SessionEvent.SESSION_STARTING = "SESSION_STARTING";
        SessionEvent.SESSION_FAILED = "SESSION_FAILED";
        SessionEvent.INCOMING_CALL = "INCOMING_CALL";
        return SessionEvent;
    })();
    a3.SessionEvent = SessionEvent;

    var CallEvent = (function () {
        function CallEvent() {
        }
        CallEvent.CALL_STARTING = "CALL_STARTING";
        CallEvent.CALL_STARTED = "CALL_STARTED";
        CallEvent.CALL_FAILED = "CALL_FAILED";
        CallEvent.CALL_FINISHED = "CALL_FINISHED";
        CallEvent.CALL_TRANSFERED = "CALL_TRANSFERED";
        CallEvent.CALL_ERROR = "CALL_ERROR";
        CallEvent.SDP_OFFER = "SDP_OFFER";
        CallEvent.SDP_ANSWER = "SDP_ANSWER";
        return CallEvent;
    })();
    a3.CallEvent = CallEvent;

    var SioSignaling = (function () {
        function SioSignaling(listener) {
            this._url = null;
            this._service = null;
            this._socket = null;
            this._intv = 0;
            this.sessionId = "";
            this._listener = listener;
        }
        SioSignaling.prototype.start = function () {
            // this instance ready immediately
            this._notifyListener('onSignalingReady', this);
        };

        SioSignaling.prototype.setClientInfo = function (prop, value) {
            //
        };

        SioSignaling.prototype.addEndpoint = function (url) {
            this._url = url;
        };

        SioSignaling.prototype.setService = function (value) {
            this._service = value;
        };

        SioSignaling.prototype.connect = function () {
            var _this = this;
            if (this._intv) {
                clearTimeout(this._intv);
            }
            if (this._socket != null) {
            }
            this._socket = io.connect(this._url);

            this._intv = setTimeout(function () {
                _this._socket = null;
                _this._notifyListener('onSignalingConnectionFailed', _this);
            }, 3000);

            this._socket.on('connect', function () {
                clearTimeout(_this._intv);
                _this._notifyListener('onSignalingConnected', _this);
            });

            this._socket.on('error', function () {
                clearTimeout(_this._intv);
                _this._notifyListener('onSignalingConnectionFailed', _this);
            });

            this._socket.on('disconnect', function () {
                clearTimeout(_this._intv);
                _this._notifyListener('onSignalingConnectionFailed', _this);
            });

            this._socket.on('connect_failed', function () {
                clearTimeout(_this._intv);
                _this._notifyListener('onSignalingConnectionFailed', _this);
            });

            this._socket.on('reconnect_failed', function () {
                clearTimeout(_this._intv);
                _this._notifyListener('onSignalingConnectionFailed', _this);
            });

            this._socket.on('message', function (message) {
                var type = message.type;
                delete message.type;

                if (type === SessionEvent.SESSION_STARTED || type === SessionEvent.SESSION_STARTING) {
                    _this.sessionId = message.sessionId;
                }
                _this._notifyListener('onSignalingMessage', type, message);
            });
        };

        SioSignaling.prototype.open = function (username, password, challenge, code) {
            this.request("START_SESSION", { username: username, password: password });
        };

        SioSignaling.prototype.startCall = function (bUri, cc, vv) {
            this.request("START_CALL", {
                sessionId: this.sessionId,
                aUri: "",
                bUri: bUri,
                cc: cc,
                vv: vv
            });
        };

        SioSignaling.prototype.dtmf = function (callId, dtmf) {
            this.request("SEND_DTMF", {
                sessionId: this.sessionId,
                callId: callId,
                dtmf: dtmf
            });
        };

        SioSignaling.prototype.hangup = function (callId) {
            this.request("HANGUP_CALL", {
                sessionId: this.sessionId,
                callId: callId
            });

            //
            // currenly no message "CALL_FINISHED" is received
            //
            this._notifyListener('onSignalingMessage', "CALL_FINISHED", {
                sessionId: this.sessionId,
                callId: callId
            });
        };

        SioSignaling.prototype.sdpAnswer = function (callId, pointId, sdp) {
            this.request("SDP_ANSWER", {
                sessionId: this.sessionId,
                callId: callId,
                pointId: pointId,
                sdp: sdp
            });
        };

        SioSignaling.prototype.request = function (type, opt) {
            if (!opt)
                opt = {};
            opt["type"] = type;
            opt["service"] = this._service;

            switch (type) {
                case 'START_SESSION':
                case 'CLOSE_SESSION':
                    opt["typz"] = 'AuthMessage';
                    break;

                case 'START_CALL':
                case 'REJECT_CALL':
                case 'ACCEPT_CALL':
                case 'HANGUP_CALL':
                case 'SEND_DTMF':
                    opt["typz"] = 'CallMessage';
                    break;

                case 'SDP_OFFER':
                case 'SDP_ANSWER':
                    opt["typz"] = 'MediaMessage';
                    break;

                case 'JOIN_CHATROOM':
                case 'UNJOIN_CHATROOM':
                case 'CHAT_MESSAGE':
                    opt["typz"] = 'ChatMessage';
                    break;
            }

            if (type != "START_SESSION")
                opt["sessionId"] = this.sessionId;
            LOG("SENDING TO SOCKET.IO: ", type, opt);
            this._socket.emit('message', opt);
        };

        SioSignaling.prototype._notifyListener = function (callback) {
            var args = [];
            for (var _i = 0; _i < (arguments.length - 1); _i++) {
                args[_i] = arguments[_i + 1];
            }
            if (this._listener == null)
                return;
            if (typeof this._listener[callback] !== 'function')
                return;
            this._listener[callback].apply(this._listener, Array.prototype.slice.call(arguments, 1));
        };
        return SioSignaling;
    })();
    a3.SioSignaling = SioSignaling;

    var FlashSignaling = (function () {
        function FlashSignaling(_listener, _container, _flashVars) {
            var _this = this;
            this._listener = _listener;
            this._container = _container;
            this._flashVars = _flashVars;
            this.sessionId = "";
            this._endpoints = [];
            this._swf = null;
            this._service = null;
            this._flashVars = this._flashVars || {};
            var readyCallback = '__' + Math.round(Math.random() * Math.pow(10, 16));
            this._flashVars['cbReady'] = readyCallback;
            window[readyCallback] = function () {
                _this._listener.onSignalingReady(_this);
            };
            var signalingCallback = '__' + Math.round(Math.random() * Math.pow(10, 16));
            this._flashVars['cbSignaling'] = signalingCallback;
            window[signalingCallback] = function (e) {
                _this.onSignalingMessage(e.type, e);
            };
        }
        FlashSignaling.prototype.start = function () {
            var _this = this;
            // Note: value "microphoneVolume" should be named micVolume
            swfobject.embedSWF("RTMP2JS.swf", this._container, "1", "1", MIN_FLASH_VERSION, null, this._flashVars || {}, {
                'allowScriptAccess': 'always',
                'wmode': 'transparent'
            }, { id: "a3-swf-signaling", name: "a3-swf-signaling" }, function (e) {
                if (!e.success) {
                    _this._listener.onSignalingFailed(_this);
                } else {
                    _this._swf = e.ref;
                }
            });
        };

        FlashSignaling.prototype.addEndpoint = function (url) {
            this._endpoints.push(url);
        };

        FlashSignaling.prototype.setService = function (value) {
        };

        FlashSignaling.prototype.connect = function () {
            while (this._endpoints.length) {
                this._swf.addEndpoint(this._endpoints.shift());
            }
            this._swf.connect();
        };

        FlashSignaling.prototype.close = function () {
            this._swf.close();
        };

        FlashSignaling.prototype.setClientInfo = function (prop, value) {
            this._swf.setClientInfo(prop, value);
        };

        FlashSignaling.prototype.request = function (type, opt) {
            if (!opt)
                opt = {};
            opt["type"] = type;
            opt["service"] = this._service;
            if (type != "START_SESSION")
                opt["sessionId"] = this.sessionId;
            LOG("SENDING TO Flash: ", type, opt);
            this._swf.notifyMessage(opt);
        };

        FlashSignaling.prototype._createOfferFromFlashSignaling = function (data) {
            var rtmpUrls = {
                playUrlVideo: data.playUrlVideo,
                playUrlVoice: data.playUrlVoice,
                publishUrlVideo: data.publishUrlVideo,
                publishUrlVoice: data.publishUrlVoice
            };
            return rtmpUrls;
        };

        // ISignalingListener::onSignalingMessage
        FlashSignaling.prototype.onSignalingMessage = function (type, data) {
            switch (type) {
                case "CONNECTING":
                    break;

                case "CONNECTED":
                    this._listener.onSignalingConnected(this);
                    break;

                case "CONNECTION_FAILED":
                    this._listener.onSignalingConnectionFailed(this);
                    break;

                case "SESSION_STARTED":
                    this.sessionId = data.sessionId;
                    this._listener.onSignalingMessage("SESSION_STARTED", data);
                    break;

                case "SESSION_FAILED":
                    this.sessionId = "";
                    this._listener.onSignalingMessage("SESSION_FAILED", data);
                    break;

                case "CALL_STARTED":
                    // TODO: create offerer for CALL_STARTING
                    var offerSdp = this._createOfferFromFlashSignaling(data);
                    if (null != offerSdp) {
                        this._listener.onSignalingMessage("SDP_OFFER", {
                            pointId: data.callId,
                            callId: data.callId,
                            sdp: offerSdp
                        });
                    }
                    this._listener.onSignalingMessage("CALL_STARTED", { callId: data.callId });
                    break;

                default:
                    this._listener.onSignalingMessage(type, data);
            }
        };

        FlashSignaling.prototype.open = function (username, password, challenge, code) {
            this.request("START_SESSION", { username: username, password: password });
        };

        FlashSignaling.prototype.startCall = function (bUri, cc, vv) {
            this.request("START_CALL", {
                aUri: "",
                bUri: bUri,
                // ignore cc
                vv: vv
            });
        };

        FlashSignaling.prototype.hangup = function (callId) {
            this.request("HANGUP_CALL", {
                callId: callId
            });
        };

        FlashSignaling.prototype.dtmf = function (callId, dtmf) {
            this.request("SEND_DTMF_SIGNAL", {
                callId: callId,
                dtmfString: dtmf
            });
        };

        FlashSignaling.prototype.sdpAnswer = function (callId, pointId, sdp) {
            // Do nothing
        };

        FlashSignaling.prototype.accept = function (callId, av_params) {
            // TODO: this._swf.webcallRequest("accept", {callId: callId, av_params: av_params});
        };

        FlashSignaling.prototype.decline = function (callId) {
            // TODO: this._swf.webcallRequest("decline", {callId: callId});
        };
        return FlashSignaling;
    })();
    a3.FlashSignaling = FlashSignaling;
})(a3 || (a3 = {}));
/*
a3client Media

* webrtc media module
* DTMF beeper


TODO:
create Sdp parser and builer

*/
var a3;
(function (a3) {
    if (typeof LOG === "undefined")
        LOG = function () {
        };
    if (typeof ERROR === "undefined")
        ERROR = function () {
        };

    var MIN_FLASH_VERSION = '11';
    var DTMF_DURATION = 140;
    var DTMF_INTERVAL = 200;
    var VIDEO_WIDTH = 352;
    var VIDEO_HEIGHT = 288;
    var VIDEO_FRAMERATE = 15;

    var MediaEvent = (function () {
        function MediaEvent() {
        }
        MediaEvent.SDP_ANSWER = "SDP_ANSWER";
        return MediaEvent;
    })();
    a3.MediaEvent = MediaEvent;

    var HardwareState = (function () {
        function HardwareState() {
        }
        HardwareState.DISABLED = "disabled";
        HardwareState.ABSENT = "absent";
        HardwareState.ENABLED = "enabled";
        return HardwareState;
    })();
    a3.HardwareState = HardwareState;

    //
    // adapter.js
    //
    var RTCPeerConnection = null;
    var RTCSessionDescription = (window).RTCSessionDescription;
    var getUserMedia = null;
    var attachMediaStream = null;

    if (navigator["mozGetUserMedia"]) {
        RTCPeerConnection = window["mozRTCPeerConnection"];
        getUserMedia = navigator["mozGetUserMedia"].bind(navigator);
        attachMediaStream = function (element, stream) {
            element.mozSrcObject = stream;
            element.play();
        };
    } else if (navigator["webkitGetUserMedia"]) {
        // The RTCPeerConnection object.
        RTCPeerConnection = window["webkitRTCPeerConnection"];

        // Get UserMedia (only difference is the prefix).
        // Code from Adam Barth.
        getUserMedia = navigator["webkitGetUserMedia"].bind(navigator);

        // Attach a media stream to an element.
        attachMediaStream = function (element, stream) {
            element.src = window["webkitURL"].createObjectURL(stream);
        };
    } else {
        LOG("Browser does not appear to be WebRTC-capable");
    }

    var FlashMedia = (function () {
        function FlashMedia(_listener, _container, _flashVars) {
            var _this = this;
            this._listener = _listener;
            this._container = _container;
            this._flashVars = _flashVars;
            this._publishUrlVoice = undefined;
            this._publishUrlVideo = undefined;
            this._playUrlVoice = undefined;
            this._playUrlVideo = undefined;
            this._swf = null;
            var flashContainer = document.createElement('div');
            flashContainer.id = 'this-div-will-replaced-by-swf';
            this._container.appendChild(flashContainer);
            this._container = flashContainer;

            this._flashVars = this._flashVars || {};

            //this._flashVars['checkMicVolume'] = false;
            var readyCallback = '__' + Math.round(Math.random() * Math.pow(10, 16));
            this._flashVars['cbReady'] = readyCallback;
            window[readyCallback] = function () {
                _this._listener.onMediaReady(_this);
            };
            var mediaCallback = '__' + Math.round(Math.random() * Math.pow(10, 16));
            this._flashVars['cbMedia'] = mediaCallback;
            window[mediaCallback] = function (e) {
                _this._listener.onMediaMessage(e.type, e);
            };
        }
        FlashMedia.prototype.start = function () {
            var _this = this;
            // Note: value "microphoneVolume" should be named micVolume
            swfobject.embedSWF("MEDIA2JS.swf", this._container.id, "100%", "100%", MIN_FLASH_VERSION, null, this._flashVars, {
                'allowScriptAccess': 'always'
            }, { id: "a3-swf-media", name: "a3-swf-media" }, function (e) {
                if (!e.success) {
                    _this._listener.onMediaReady(null);
                } else {
                    _this._swf = e.ref;
                }
            });
        };

        FlashMedia.prototype.getCc = function () {
            return {
                userAgent: "FlashPlayer",
                audio: ["speex/8000"],
                video: ["H264/90000"]
            };
        };

        FlashMedia.prototype.setMicrophoneVolume = function (value) {
            this._swf.microphoneVolume(value);
        };

        FlashMedia.prototype.setSoundVolume = function (value) {
            this._swf.soundVolume(value);
        };

        FlashMedia.prototype.muteMicrophone = function (value) {
            this._swf.muteMicrophone(value);
        };

        FlashMedia.prototype.muteSound = function (value) {
            this._swf.muteSound(value);
        };

        FlashMedia.prototype.checkHardware = function (enableVideo) {
            // TODO: implements this
            this._swf.checkHardware();
        };

        FlashMedia.prototype.setOfferSdp = function (callId, pointId, offerSdp) {
            // currently offerSdp in form
            //{
            //  playUrlVideo: [...]
            //  playUrlVoice: [...]
            //  publishUrlVideo: [...]
            //  publishUrlVoice: [...]
            //}
            this._publishUrlVoice = offerSdp.publishUrlVoice;
            this._publishUrlVideo = offerSdp.publishUrlVideo;
            this._playUrlVoice = offerSdp.playUrlVoice;
            this._playUrlVideo = offerSdp.playUrlVideo;

            this._swf.publish(this._publishUrlVoice, this._publishUrlVideo);
            this._swf.subscribe(this._playUrlVoice, this._playUrlVideo);
        };

        FlashMedia.prototype.dispose = function () {
            this._swf.unpublish();
            this._swf.unsubscribe();
        };

        FlashMedia.prototype.playDtmf = function (dtmf) {
            this._swf.playDtmf(dtmf);
        };

        FlashMedia.prototype.playRBT = function () {
            this._swf.playRBT();
        };

        FlashMedia.prototype.stopRBT = function () {
            this._swf.stopRBT();
        };
        return FlashMedia;
    })();
    a3.FlashMedia = FlashMedia;

    /**
    * WebrtcMedia
    *   webrtc media implementation
    *
    */
    var WebrtcMedia = (function () {
        function WebrtcMedia(_listener, _container) {
            this._listener = _listener;
            this._container = _container;
            this._localStream = undefined;
            this._remoteStream = undefined;
            this._remoteVideoStream = undefined;
            this.__pc = null;
            this.__pcVideo = null;
            this._localVideo = null;
            this._remoteVideo = null;
            this._localAudio = null;
            this._remoteAudio = null;
            this._soundVolume = 0.8;
            this._micVolume = 1;
            this._dtmfPlayer = new DtmfPlayer();
            this._rbtPlayer = new RBTPlayer();
            this._remoteVideo = document.createElement("video");
            this._remoteVideo.style.width = '100%';
            this._remoteVideo.style.height = '100%';
            this._remoteVideo.setAttribute('autoplay', 'autoplay');

            this._remoteAudio = document.createElement("audio");
            this._remoteAudio.style.width = '100%';
            this._remoteAudio.style.height = '100%';
            this._remoteAudio.setAttribute('autoplay', 'autoplay');

            this._container.appendChild(this._remoteVideo);
            this._container.appendChild(this._remoteAudio);
        }
        WebrtcMedia.prototype.start = function () {
            // notify ready immediately
            this._notifyReady();
        };

        WebrtcMedia.prototype._notifyReady = function () {
            if (this._listener && typeof (this._listener.onMediaReady) === "function")
                this._listener.onMediaReady(this);
        };

        WebrtcMedia.prototype._notify = function (type, data) {
            if (this._listener && typeof (this._listener.onMediaMessage) === "function")
                try  {
                    this._listener.onMediaMessage(type, data);
                } catch (err) {
                    ERROR(err);
                }
        };

        WebrtcMedia.prototype.getLocalAudioTrack = function () {
            return this._localStream && this._localStream.getAudioTracks && this._localStream.getAudioTracks().length && this._localStream.getAudioTracks()[0];
        };

        WebrtcMedia.prototype.setMicrophoneVolume = function (value) {
            //var audioTrack = this.getLocalAudioTrack();
            this._notify("HardwareEvent.MICROPHONE_VOLUME_CHANGED", { data: value });
        };

        WebrtcMedia.prototype.setSoundVolume = function (value) {
            this._soundVolume = value;
            this._remoteAudio.volume = value;
            this._notify("HardwareEvent.SOUND_VOLUME_CHANGED", { data: value });
        };

        WebrtcMedia.prototype.muteMicrophone = function (value) {
            this.getLocalAudioTrack().enabled = !value;
        };

        WebrtcMedia.prototype.muteSound = function (value) {
            this._remoteAudio.volume = (value ? 0 : this._soundVolume);
        };

        WebrtcMedia.prototype.getCc = function () {
            return {
                "userAgent": "Chrome",
                "audio": ["PCMA/8000"],
                "video": ["VP8/90000"],
                "profile": "RTP/SAVPF",
                "ice": true,
                "rtcpMux": true,
                "ssrcRequired": true,
                "bundle": false
            };
        };

        WebrtcMedia.prototype.dispose = function () {
            if (this.__pcVideo)
                this.__pcVideo.close();
            this.__pcVideo = null;
            if (this.__pc)
                this.__pc.close();
            this.__pc = null;
            try  {
                this._remoteStream.getVideoTracks()[0].stop();
            } catch (e) {
                LOG("Trouble on dispose video");
            }
            try  {
                this._remoteStream.getAudioTracks()[0].stop();
            } catch (e) {
                LOG("Trouble on dispose audio");
            }
            try  {
                this._localStream.getVideoTracks()[0].stop();
            } catch (e) {
                LOG("Trouble on dispose video");
            }
            try  {
                this._localStream.getAudioTracks()[0].stop();
            } catch (e) {
                LOG("Trouble on dispose audio");
            }
        };

        WebrtcMedia.prototype.checkHardware = function (enableVideo) {
            var _this = this;
            this._notify("HardwareEvent.HARDWARE_STATE", {
                data: {
                    microphone: { state: HardwareState.DISABLED },
                    camera: { state: HardwareState.DISABLED },
                    userDefined: false
                }
            });

            var opts = { audio: true };
            if (enableVideo) {
                opts.video = {
                    mandatory: {
                        "minWidth": "320",
                        "maxWidth": "1280",
                        "minHeight": "180",
                        "maxHeight": "720",
                        "minFrameRate": "5"
                    },
                    optional: [
                        { width: VIDEO_WIDTH },
                        { height: VIDEO_HEIGHT },
                        { frameRate: VIDEO_FRAMERATE },
                        { facingMode: "user" }
                    ]
                };
            }
            getUserMedia(opts, function (stream) {
                var audioTracks = stream.getAudioTracks();
                var videoTracks = stream.getVideoTracks();
                _this._localStream = stream;
                _this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", {
                    data: {
                        microphone: {
                            state: audioTracks.length ? HardwareState.ENABLED : HardwareState.ABSENT,
                            wtfName: audioTracks.length ? audioTracks[0].label : ""
                        },
                        camera: {
                            state: videoTracks.length ? HardwareState.ENABLED : HardwareState.ABSENT,
                            wtfName: videoTracks.length ? videoTracks[0].label : ""
                        },
                        userDefined: true
                    }
                });
            }, function (err) {
                LOG("error getting mediastream", err);
                _this._localStream = undefined;
                _this._notify("HardwareEvent.HARDWARE_STATE", {
                    data: {
                        microphone: { state: HardwareState.DISABLED },
                        camera: { state: HardwareState.DISABLED },
                        userDefined: true
                    }
                });
            });
        };

        WebrtcMedia.prototype.__getAudioPeerConnection = function () {
            var _this = this;
            assert(this._localStream);
            if (this.__pc)
                return this.__pc;
            var pc_config = { "iceServers": [] };
            var pc_constraints = { "optional": [{ "DtlsSrtpKeyAgreement": false }] };
            this.__pc = new RTCPeerConnection(pc_config, pc_constraints);
            this.__pc.onaddstream = function (event) {
                _this._remoteStream = event.stream;
                attachMediaStream(_this._remoteAudio, event.stream);
            };
            this.__pc.onremovestream = function (event) {
            };
            this.__pc.addStream(this._localStream);
            return this.__pc;
        };

        WebrtcMedia.prototype.__getVideoPeerConnection = function () {
            var _this = this;
            assert(this._localStream);
            if (this.__pcVideo)
                return this.__pcVideo;
            var pc_config = { "iceServers": [] };
            var pc_constraints = { "optional": [{ "DtlsSrtpKeyAgreement": false }] };
            this.__pcVideo = new RTCPeerConnection(pc_config, pc_constraints);
            this.__pcVideo.onaddstream = function (event) {
                _this._remoteVideoStream = event.stream;
                attachMediaStream(_this._remoteVideo, event.stream);
            };
            this.__pcVideo.onremovestream = function (event) {
            };
            this.__pcVideo.addStream(this._localStream);
            return this.__pcVideo;
        };

        WebrtcMedia.prototype.setOfferSdp = function (callId, pointId, offerSdp) {
            var _this = this;
            LOG("Offer Sdp=\n" + offerSdp);
            var headLines = [], audioLines = [], videoLines = [];
            var lines = offerSdp.split("\r\n");
            var state = 0;
            for (var i = 0; i < lines.length; i++) {
                var line = lines[i];
                if (line.match(/^m=audio/))
                    state = 1;
                if (line.match(/^m=video/))
                    state = 2;
                if (line) {
                    switch (state) {
                        case 0:
                            headLines.push(line);
                            break;
                        case 1:
                            audioLines.push(line);
                            break;
                        case 2:
                            videoLines.push(line);
                            break;
                    }
                }
            }
            var head = headLines.join("\r\n") + "\r\n", audio = audioLines.length ? audioLines.join("\r\n") + "\r\n" : "", video = videoLines.length ? videoLines.join("\r\n") + "\r\n" : "";

            var audioAnswerSdp = "";
            var videoAnswerSdp = "";

            var answerReady = function () {
                if (audio && !audioAnswerSdp || video && !videoAnswerSdp) {
                    return;
                }

                var answerLines = [];
                var headSdp = audioAnswerSdp || videoAnswerSdp;

                var lines = headSdp.split("\r\n");
                var state = 0;
                for (var i = 0; i < lines.length; i++) {
                    var line = lines[i];
                    if (line.match(/^m=audio/))
                        state = 1;
                    if (line.match(/^m=video/))
                        state = 2;
                    if (line) {
                        switch (state) {
                            case 0:
                                if (!line.match(/^a=msid-semantic/))
                                    answerLines.push(line);
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                        }
                    }
                }

                if (audioAnswerSdp) {
                    var lines = audioAnswerSdp.split("\r\n");
                    var state = 0;
                    for (var i = 0; i < lines.length; i++) {
                        var line = lines[i];
                        if (line.match(/^m=audio/))
                            state = 1;
                        if (line.match(/^m=video/))
                            state = 2;
                        if (line) {
                            switch (state) {
                                case 0:
                                    break;
                                case 1:
                                    answerLines.push(line);
                                    break;
                                case 2:
                                    break;
                            }
                        }
                    }
                }

                if (videoAnswerSdp) {
                    var lines = videoAnswerSdp.split("\r\n");
                    var state = 0;
                    for (var i = 0; i < lines.length; i++) {
                        var line = lines[i];
                        if (line.match(/^m=audio/))
                            state = 1;
                        if (line.match(/^m=video/))
                            state = 2;
                        if (line) {
                            switch (state) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    answerLines.push(line);
                                    break;
                            }
                        }
                    }
                }

                var answerSdp = answerLines.join("\r\n") + "\r\n";
                LOG("We have FINISHED building answer SDP\n" + answerSdp);
                _this._listener.onMediaMessage(MediaEvent.SDP_ANSWER, {
                    callId: callId,
                    pointId: pointId,
                    sdp: answerSdp
                });
            };

            var audioAnswerReady = function (sdp) {
                audioAnswerSdp = sdp;
                answerReady();
            };

            var videoAnswerReady = function (sdp) {
                videoAnswerSdp = sdp;
                answerReady();
            };

            if (audio) {
                var pcAudio = this.__getAudioPeerConnection();
                var mediaConstraints = { 'mandatory': { 'OfferToReceiveAudio': true, 'OfferToReceiveVideo': false } };
                var remoteSDP = new RTCSessionDescription({ sdp: /*offerSdp*/ head + audio, type: 'offer' });
                pcAudio.setRemoteDescription(remoteSDP);
                LOG("pc.CreatingAnswer: AUDIO", pcAudio);
                pcAudio.createAnswer(function (localSDP) {
                    LOG("Answerer SDP (audio)=\n", localSDP.sdp);
                    pcAudio.setLocalDescription(localSDP);
                    audioAnswerReady(localSDP.sdp);
                }, null, mediaConstraints);
            }

            if (video) {
                var pcVideo = this.__getVideoPeerConnection();
                var mediaConstraints = { 'mandatory': { 'OfferToReceiveAudio': false, 'OfferToReceiveVideo': true } };
                var remoteSDP = new RTCSessionDescription({ sdp: /*offerSdp*/ head + video, type: 'offer' });
                pcVideo.setRemoteDescription(remoteSDP);
                LOG("pc.CreatingAnswer: VIDEO", pcVideo);
                pcVideo.createAnswer(function (localSDP) {
                    LOG("Answerer SDP (video)=\n", localSDP.sdp);
                    pcVideo.setLocalDescription(localSDP);
                    videoAnswerReady(localSDP.sdp);
                }, null, mediaConstraints);
            }
        };
        WebrtcMedia.prototype.playDtmf = function (dtmf) {
            this._dtmfPlayer.playDtmf(dtmf);
        };

        WebrtcMedia.prototype.playRBT = function () {
            this._rbtPlayer.play();
        };

        WebrtcMedia.prototype.stopRBT = function () {
            this._rbtPlayer.stop();
        };
        return WebrtcMedia;
    })();
    a3.WebrtcMedia = WebrtcMedia;

    /**
    *  DtmfPlayer
    *    play dtmf codes
    *
    *
    */
    var ToneGenerator = (function () {
        function ToneGenerator(audioContext, freq) {
            this.audioContext = audioContext;
            this.freq = freq;
            var volume = audioContext.createGainNode();
            volume.gain.value = 0.2;
            volume.connect(audioContext.destination);
            this.gainNode = volume;

            this.oscillator = this.audioContext.createOscillator();
            this.oscillator.type = 0;
            this.oscillator.frequency.value = freq;
            this.oscillator.noteOn(0);
        }
        ToneGenerator.prototype.play = function () {
            this.oscillator.connect(this.gainNode);
        };
        ToneGenerator.prototype.pause = function () {
            this.oscillator.disconnect();
        };
        return ToneGenerator;
    })();

    /**
    * Ringback tone player
    */
    var RBTPlayer = (function () {
        function RBTPlayer() {
            this._oscillator = null;
            this._cnt = 0;
            if (window["webkitAudioContext"]) {
                var audioContext = new window["webkitAudioContext"]();
                this._oscillator = new ToneGenerator(audioContext, 425);
            }
        }
        RBTPlayer.prototype.play = function () {
            var _this = this;
            this._intv = setInterval(function () {
                if (!(_this._cnt % 5)) {
                    _this._oscillator.play();
                } else if (!((_this._cnt - 1) % 5)) {
                    _this._oscillator.pause();
                }
                _this._cnt++;
            }, 1000);
        };

        RBTPlayer.prototype.stop = function () {
            this._oscillator.pause();
            clearInterval(this._intv);
        };
        return RBTPlayer;
    })();

    var DtmfPlayer = (function () {
        function DtmfPlayer() {
            var _this = this;
            this._audioContext = null;
            this._oscillators = {};
            this._queue = [];
            this._timerId = null;
            if (window["webkitAudioContext"]) {
                this._audioContext = new window["webkitAudioContext"]();
                this._oscillators = {
                    "697": new ToneGenerator(this._audioContext, 697),
                    "770": new ToneGenerator(this._audioContext, 770),
                    "852": new ToneGenerator(this._audioContext, 852),
                    "941": new ToneGenerator(this._audioContext, 941),
                    "1209": new ToneGenerator(this._audioContext, 1209),
                    "1336": new ToneGenerator(this._audioContext, 1336),
                    "1477": new ToneGenerator(this._audioContext, 1477),
                    "1633": new ToneGenerator(this._audioContext, 1633)
                };
            }
            this._timerId = window.setInterval(function () {
                if (_this._queue.length) {
                    _this.__playDtmfCharacter(_this._queue.splice(0, 1)[0]);
                }
            }, DTMF_INTERVAL);
        }
        DtmfPlayer.prototype.playDtmf = function (dtmf) {
            for (var i = 0; i < dtmf.length; i++) {
                var c = dtmf.toString().charAt(i).toUpperCase();
                if (DtmfPlayer._tones.hasOwnProperty(c)) {
                    this._queue.push(c);
                } else {
                }
            }
        };

        DtmfPlayer.prototype.__playDtmfCharacter = function (c) {
            if (DtmfPlayer._tones.hasOwnProperty(c) && this._audioContext) {
                var freq1 = DtmfPlayer._tones[c][1];
                var freq2 = DtmfPlayer._tones[c][0];

                var oscillator1 = this._oscillators[freq1];
                var oscillator2 = this._oscillators[freq2];

                oscillator1.play();
                oscillator2.play();
                window.setTimeout(function () {
                    oscillator1.pause();
                    oscillator2.pause();
                }, DTMF_DURATION);
            }
        };
        DtmfPlayer._tones = {
            "1": [697, 1209],
            "2": [697, 1336],
            "3": [697, 1477],
            "A": [697, 1633],
            "4": [770, 1209],
            "5": [770, 1336],
            "6": [770, 1477],
            "B": [770, 1633],
            "7": [852, 1209],
            "8": [852, 1336],
            "9": [852, 1477],
            "C": [852, 1633],
            "*": [941, 1209],
            "0": [941, 1336],
            "#": [941, 1477],
            "D": [941, 1633]
        };
        return DtmfPlayer;
    })();
})(a3 || (a3 = {}));
/// <reference path="signaling.ts" />
/// <reference path="media.ts" />
var a3;
(function (a3) {
    if (typeof assert !== "function")
        assert = function () {
        };
    if (typeof ERROR !== "function")
        ERROR = function () {
            throw ("Error");
        };
    if (typeof WARN !== "function")
        WARN = function () {
        };
    if (typeof LOG !== "function")
        LOG = function () {
        };

    var STARTING_TIMEOUT = 10000;

    var Event = (function () {
        function Event() {
        }
        Event.START = "e:START";
        Event.SIGNALING_READY = "e:SIGNALING_READY";
        Event.SIGNALING_FAILED = "e:SIGNALING_FAILED";
        Event.MEDIA_READY = "e:MEDIA_READY";
        Event.MEDIA_FAILED = "e:MEDIA_FAILED";
        Event.START_TIMEOUT = "e:START_TIMEOUT";
        Event.CONNECT = "e:CONNECT";
        Event.CONNECTED = "e:CONNECTED";
        Event.CONNECTION_FAILED = "e:CONNECTION_FAILED";
        Event.HARDWARE_STATE_CHANGED = "HardwareEvent.HARDWARE_STATE";
        Event.SOUND_VOLUME_CHANGED = "HardwareEvent.SOUND_VOLUME_CHANGED";
        Event.TRANSFER_FAILED = "TRANSFER_FAILED";
        Event.CANCEL = "CANCEL";

        Event.START_INCOMING = "e:START_INCOMING";
        Event.ACCEPT_CALL = "e:ACCEPT_CALL";
        Event.DECLINE_CALL = "e:DECLINE_CALL";
        return Event;
    })();
    a3.Event = Event;

    var State = (function () {
        function State() {
        }
        State.STARTING = "s:STARTING";
        State.SIGNALING_READY = "s:SIGNALING_READY";
        State.MEDIA_READY = "s:MEDIA_READY";
        State.STARTED = "s:STARTED";
        State.FAILED = "s:FAILED";

        State.CONNECTING = "s:CONNECTING";
        State.CONNECTED = "s:CONNECTED";
        State.CONNECTION_FAILED = "s:CONNECTION_FAILED";
        State.SESSION_STARTING = "s:SESSION_STARTING";
        State.SESSION_STARTED = "s:SESSION_STARTED";
        State.SESSION_FAILED = "s:SESSION_FAILED";
        State.DISCONNECTED = "s:DISCONNECTED";
        return State;
    })();
    a3.State = State;
    var CallState = (function () {
        function CallState() {
        }
        CallState.STARTING = "Call:STARTING";
        CallState.RINGING = "Call:RINGING";
        CallState.PROGRESS = "Call:PROGRESS";
        CallState.FINISHED = "Call:FINISHED";
        CallState.FAILED = "Call:FAILED";
        return CallState;
    })();
    a3.CallState = CallState;

    var CallType = (function () {
        function CallType() {
        }
        CallType.AUDIO = "audio";
        CallType.VIDEO = "video";
        CallType.BOTH = "audio-video";
        return CallType;
    })();
    a3.CallType = CallType;

    var Call = (function () {
        function Call(_vv, media, signaling) {
            this._vv = _vv;
            this.media = media;
            this.signaling = signaling;
            this._uid = null;
            this._state = null;
            this._duration = 0;
            this._durationTimerHandle = null;
            this._listener = null;
        }
        Call.prototype.setListener = function (listener) {
            this._listener = listener;
        };

        Call.prototype.getId = function () {
            return this._uid;
        };
        Call.prototype.setId = function (id) {
            this._uid = id;
        };
        Call.prototype.isAudio = function () {
            return this._vv[0] === true;
        };
        Call.prototype.isVideo = function () {
            return this._vv[1] === true;
        };

        Call.prototype.accept = function () {
            this.event(Event.ACCEPT_CALL, null);
        };
        Call.prototype.decline = function () {
            this.event(Event.DECLINE_CALL, null);
        };
        Call.prototype.start = function () {
            this.event(Event.START, null);
        };
        Call.prototype.startIncoming = function () {
            this.event(Event.START_INCOMING, null);
        };

        Call.prototype.sendDTMF = function (dtmf) {
            this.signaling.dtmf(this._uid, dtmf);
        };
        Call.prototype.getState = function () {
            return this._state;
        };

        Call.prototype.hangup = function () {
            assert(this._uid, "Call::hangup: self._uid === null");
            this.signaling.hangup(this._uid);
        };
        Call.prototype.remove = function () {
            LOG("Remove CALL");
        };
        Call.prototype.setMicrophoneVolume = function (value) {
            this.media.setMicrophoneVolume(value);
        };
        Call.prototype.setSoundVolume = function (value) {
            this.media.setSoundVolume(value);
        };

        Call.prototype.onEnterStateStarting = function () {
            this.media.playRBT();
            this._notifyListener('onCallStarting');
        };

        Call.prototype.onEnterStateRinging = function () {
            this._notifyListener('onCallRinging');
        };
        Call.prototype.onEnterStateProgress = function () {
            this.media.stopRBT();
            this._notifyListener('onCallStarted');
        };

        Call.prototype.onEnterStateFinished = function () {
            this.media.stopRBT();
            this.media.dispose();
            this._notifyListener('onCallFinished');
        };
        Call.prototype.onEnterStateFailed = function () {
            this.media.stopRBT();
            this.media.dispose();
            this._notifyListener('onCallFailed');
        };

        Call.prototype._notifyListener = function (callback) {
            if (this._listener == null)
                return;
            if (typeof this._listener[callback] !== 'function')
                return;
            this._listener[callback](this);
        };

        Call.prototype._setState = function (newState) {
            if (this._state !== newState) {
                this._state = newState;
                switch (this._state) {
                    case CallState.RINGING:
                        this.onEnterStateRinging();
                        break;
                    case CallState.STARTING:
                        this.onEnterStateStarting();
                        break;
                    case CallState.PROGRESS:
                        this.onEnterStateProgress();
                        break;
                    case CallState.FINISHED:
                        this.onEnterStateFinished();
                        break;
                    case CallState.FAILED:
                        this.onEnterStateFailed();
                        break;
                }
            }
        };
        Call.prototype._unhandledEvent = function (event, opt) {
            WARN("Unhandled event", event, " in state=", this._state);
        };
        Call.prototype._startTimer = function () {
            var _this = this;
            if (this._durationTimerHandle)
                return;
            var startTime = new Date().valueOf();
            this._durationTimerHandle = window.setInterval(function () {
                var time = new Date().valueOf();
                var duration = Math.round((time - startTime) / 1000);
                if (_this._duration !== duration) {
                    _this._duration = duration;
                    _this._listener.onDurationChanged(_this, _this._duration);
                }
            }, 250);
        };
        Call.prototype._stopTimer = function () {
            if (this._durationTimerHandle) {
                window.clearInterval(this._durationTimerHandle);
                this._durationTimerHandle = null;
            }
        };
        Call.prototype.event = function (event, opt) {
            LOG("SM-CALL: state=", this._state, " event=", event, "opt=", opt);

            switch (this._state) {
                case null:
                    if (event === Event.START) {
                        this._setState(CallState.STARTING);
                    } else if (event === Event.START_INCOMING) {
                        this._setState(CallState.RINGING);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case CallState.STARTING:
                    if (event === a3.CallEvent.CALL_STARTED) {
                        this._startTimer();
                        this._setState(CallState.PROGRESS);
                    } else if (event === a3.CallEvent.CALL_STARTING) {
                        // OK
                    } else if (event === a3.CallEvent.SDP_OFFER) {
                        this.__onSdpOffer(opt);
                    } else if (event === a3.CallEvent.CALL_FAILED || event === a3.CallEvent.CALL_ERROR) {
                        this._setState(CallState.FAILED);
                    } else if (event === a3.CallEvent.CALL_FINISHED) {
                        this._setState(CallState.FINISHED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case CallState.RINGING:
                    if (event === a3.CallEvent.CALL_STARTED) {
                        this._startTimer();
                        this._setState(CallState.PROGRESS);
                    } else if (event === Event.ACCEPT_CALL) {
                        this._startTimer();
                        this._setState(CallState.PROGRESS);
                    } else if (event === Event.DECLINE_CALL) {
                        this._setState(CallState.FINISHED);
                    } else if (event === a3.CallEvent.CALL_FAILED || event === a3.CallEvent.CALL_ERROR) {
                        this._setState(CallState.FAILED);
                    } else if (event === a3.CallEvent.CALL_FINISHED) {
                        this._setState(CallState.FINISHED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case CallState.PROGRESS:
                    if (event === a3.CallEvent.CALL_FINISHED) {
                        this._stopTimer();
                        this._setState(CallState.FINISHED);
                    } else if (event === a3.CallEvent.CALL_FAILED || event === a3.CallEvent.CALL_ERROR) {
                        this._setState(CallState.FAILED);
                    } else
                        this._unhandledEvent(event, opt);
                    break;

                case CallState.FINISHED:
                    this._unhandledEvent(event, opt);
                    break;
            }
        };
        Call.prototype.__onSdpOffer = function (opt) {
            var pointId = opt.pointId;
            var callId = opt.callId;
            var offerSdp = opt.sdp;
            this.media.setOfferSdp(callId, pointId, offerSdp);
        };
        return Call;
    })();
    a3.Call = Call;

    var Communicator = (function () {
        function Communicator() {
            this.sessionId = null;
            this.calls = [];
            this.signaling = null;
            this.media = null;
            this._startingTimeout = 0;
            this._state = null;
            this._microphoneState = a3.HardwareState.DISABLED;
            this._cameraState = a3.HardwareState.DISABLED;
        }
        Communicator.prototype.setFactory = function (factory) {
            this.factory = factory;
        };

        // communicator subject
        Communicator.prototype.onCommunicatorStarting = function () {
        };
        Communicator.prototype.onCommunicatorStarted = function () {
        };
        Communicator.prototype.onCommunicatorFailed = function () {
        };

        // signaling connection
        Communicator.prototype.onConnecting = function () {
        };
        Communicator.prototype.onConnected = function () {
        };
        Communicator.prototype.onConnectionFailed = function () {
        };

        // hardware
        Communicator.prototype.onCheckHardwareSettings = function () {
        };
        Communicator.prototype.onCheckHardwareReady = function () {
        };
        Communicator.prototype.onCheckHardwareFailed = function () {
        };
        Communicator.prototype.onSoundVolumeChanged = function (value) {
        };

        // session
        Communicator.prototype.onSessionStarting = function () {
        };
        Communicator.prototype.onSessionStarted = function () {
        };
        Communicator.prototype.onSessionFailed = function () {
        };

        // calls
        Communicator.prototype.onCallStarting = function (call) {
        };
        Communicator.prototype.onCallStarted = function (call) {
        };
        Communicator.prototype.onCallFinished = function (call) {
        };
        Communicator.prototype.onCallFailed = function (call) {
        };
        Communicator.prototype.onIncomingCall = function (call) {
        };
        Communicator.prototype.onDurationChanged = function (call, duration) {
        };

        Communicator.prototype.start = function () {
            var _this = this;
            if (this._state === null) {
                this._startingTimeout = setTimeout(function () {
                    _this.event(Event.START_TIMEOUT, null);
                }, STARTING_TIMEOUT);

                this._setState(State.STARTING);

                this.media = this.factory.createMedia(this);
                if (this.media == null) {
                    this.event(Event.MEDIA_FAILED, null);
                } else {
                    this.media.start();
                }

                this.signaling = this.factory.createSignaling(this);
                if (this.signaling == null) {
                    this.event(Event.SIGNALING_FAILED, null);
                } else {
                    this.signaling.start();
                }
            } else {
                throw new Error("Method `start` in wrong state");
            }
        };

        Communicator.prototype.connect = function () {
            this.event(Event.CONNECT, null);
            this.signaling.connect();
        };

        Communicator.prototype.open = function (phone, password, challenge, code) {
            this.event(a3.SessionEvent.SESSION_STARTING, null);
            this.signaling.open(phone, password, challenge, code);
        };

        Communicator.prototype.close = function () {
        };

        Communicator.prototype.startCall = function (destination, vv) {
            if (this._state !== State.SESSION_STARTED) {
                throw new Error("Start the call in wrong state " + this._state);
            }

            var call = this._createCallInstance(vv);
            call.setListener(this);
            this._addCall(call);
            call.start();
            var cc = this.media.getCc();
            this.signaling.startCall(destination, cc, vv);
        };

        Communicator.prototype.setSoundVolume = function (value) {
            this.media.setSoundVolume(value);
        };

        Communicator.prototype.getMicrophoneState = function () {
            return this._microphoneState;
        };

        Communicator.prototype.getCameraState = function () {
            return this._cameraState;
        };

        Communicator.prototype._createCallInstance = function (vv) {
            return new Call(vv, this.media, this.signaling);
        };

        Communicator.prototype._getCallById = function (id) {
            for (var i = 0; i < this.calls.length; i++)
                if (this.calls[i].getId() === id)
                    return this.calls[i];
            return null;
        };

        Communicator.prototype._addCall = function (call) {
            this.calls.push(call);
        };

        Communicator.prototype._removeCall = function (call) {
            for (var i = 0; i < this.calls.length; i++)
                if (call === this.calls[i]) {
                    this.calls.splice(i, 1);
                    call.remove();
                    return;
                }
            assert(0);
        };

        Communicator.prototype._setState = function (newState) {
            if (this._state !== newState) {
                this._state = newState;
                switch (this._state) {
                    case State.STARTING:
                        this.onCommunicatorStarting();
                        break;
                    case State.STARTED:
                        this.onCommunicatorStarted();
                        break;
                    case State.FAILED:
                        this.onCommunicatorFailed();
                        break;
                    case State.CONNECTING:
                        this.onConnecting();
                        break;
                    case State.CONNECTED:
                        this.onConnected();
                        break;
                    case State.CONNECTION_FAILED:
                        this.onConnectionFailed();
                        break;
                    case State.SESSION_STARTING:
                        this.onSessionStarting();
                        break;
                    case State.SESSION_STARTED:
                        this.onSessionStarted();
                        break;
                    case State.SESSION_FAILED:
                        this.onSessionFailed();
                        break;
                    case State.DISCONNECTED:
                        this.onConnectionFailed();
                        break;
                }
            }
        };

        Communicator.prototype._setHardwareState = function (microphoneState, cameraState) {
            this._microphoneState = microphoneState;
            this._cameraState = cameraState;
        };

        Communicator.prototype.getState = function () {
            return this._state;
        };

        Communicator.prototype._unhandledEvent = function (event, opt) {
            WARN("Unhandled event", event, " in state=", this._state);
        };

        Communicator.prototype.event = function (event, opt) {
            LOG("SM: state=", this._state, " event=", event, "opt=", opt);

            if (event == Event.SOUND_VOLUME_CHANGED) {
                this.onSoundVolumeChanged(opt.data);
                return;
            }

            var call = null;
            if (opt && opt.callId) {
                call = this._getCallById(opt.callId) || this._getCallById(null);
                if (call && !call.getId()) {
                    LOG("Setting call uid=" + opt.callId);
                    call.setId(opt.callId);
                }
            }

            switch (this._state) {
                case State.STARTING:
                    if (event === Event.SIGNALING_READY) {
                        this._setState(State.SIGNALING_READY);
                    } else if (event === Event.MEDIA_READY) {
                        this._setState(State.MEDIA_READY);
                    } else if (event === Event.SIGNALING_FAILED || event === Event.MEDIA_FAILED || event === Event.START_TIMEOUT) {
                        this._setState(State.FAILED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.SIGNALING_READY:
                    if (event === Event.MEDIA_READY) {
                        clearInterval(this._startingTimeout);
                        this._setState(State.STARTED);
                    } else if (event === Event.MEDIA_FAILED || event === Event.START_TIMEOUT) {
                        this._setState(State.FAILED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.MEDIA_READY:
                    if (event === Event.SIGNALING_READY) {
                        clearInterval(this._startingTimeout);
                        this._setState(State.STARTED);
                    } else if (event === Event.SIGNALING_FAILED || event === Event.START_TIMEOUT) {
                        this._setState(State.FAILED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.STARTED:
                    clearInterval(this._startingTimeout);

                case State.CONNECTION_FAILED:
                    if (event === Event.CONNECT) {
                        this._setState(State.CONNECTING);
                    } else
                        this._unhandledEvent(event, opt);
                    break;

                case State.CONNECTING:
                    if (event === Event.CONNECTED) {
                        this._setState(State.CONNECTED);
                    } else if (event === Event.CONNECTION_FAILED) {
                        this._setState(State.CONNECTION_FAILED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.CONNECTED:
                    if (event === Event.CONNECTION_FAILED) {
                        this._setState(State.CONNECTION_FAILED);
                    } else if (event === a3.SessionEvent.SESSION_STARTING) {
                        this._setState(State.SESSION_STARTING);
                    } else if (event === a3.SessionEvent.SESSION_FAILED) {
                        this._setState(State.SESSION_FAILED);
                    } else if (event === Event.HARDWARE_STATE_CHANGED) {
                        this._onHardwareStateChanged(opt);
                    } else if (event === Event.MEDIA_READY) {
                        this.media.checkHardware(true);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.SESSION_STARTED:
                    if (event === a3.CallEvent.CALL_STARTING || event === a3.CallEvent.CALL_STARTED || event === a3.CallEvent.SDP_OFFER) {
                        assert(call);
                        call.event(event, opt);
                    } else if (event === a3.CallEvent.CALL_FAILED || event === a3.CallEvent.CALL_FINISHED || event === a3.CallEvent.CALL_ERROR) {
                        if (call) {
                            call.event(event, opt);
                            this._removeCall(call);
                            LOG("Calls count = ", this.calls.length);
                        } else {
                            LOG("Warning: Trying to remove unexistent call");
                        }
                    } else if (event === a3.CallEvent.SDP_ANSWER) {
                        assert(call);
                        this.signaling.sdpAnswer(opt.callId, opt.pointId, opt.sdp);
                    } else if (event === a3.SessionEvent.INCOMING_CALL) {
                        var call = this._createCallInstance(opt.vv);
                        this._addCall(call);
                        call.startIncoming();
                        this.onIncomingCall(call);
                    } else if (event === Event.HARDWARE_STATE_CHANGED) {
                        this._onHardwareStateChanged(opt);
                    } else if (event === Event.MEDIA_READY) {
                        this.media.checkHardware(true);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.SESSION_STARTING:
                    if (event === Event.CONNECTION_FAILED) {
                        this._setState(State.CONNECTION_FAILED);
                    } else if (event === a3.SessionEvent.SESSION_STARTED) {
                        this._setState(State.SESSION_STARTED);
                    } else if (event === a3.SessionEvent.SESSION_FAILED) {
                        this._setState(State.SESSION_FAILED);
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.FAILED:
                default:
                    this._unhandledEvent(event, opt);
                    break;
            }
        };

        Communicator.prototype._onHardwareStateChanged = function (opt) {
            if (opt.data.microphone.state === a3.HardwareState.ENABLED) {
                this._setHardwareState(opt.data.microphone.state, opt.data.camera.state);
                this.onCheckHardwareReady();
            } else if (opt.data.microphone.state === a3.HardwareState.DISABLED) {
                if (opt.data.userDefined) {
                    // user declined microphone
                    this.onCheckHardwareFailed();
                } else {
                    this.onCheckHardwareSettings();
                }
            }
        };

        Communicator.prototype.onSignalingReady = function (o) {
            this.signaling = o;
            this.event(Event.SIGNALING_READY, o);
        };

        Communicator.prototype.onSignalingFailed = function (o) {
            this.event(Event.SIGNALING_FAILED, o);
        };

        Communicator.prototype.onSignalingConnected = function (o) {
            this.event(Event.CONNECTED, o);
        };

        Communicator.prototype.onSignalingConnectionFailed = function (o) {
            this.signaling = o;
            this.event(Event.CONNECTION_FAILED, o);
        };

        Communicator.prototype.onSignalingMessage = function (type, opt) {
            try  {
                this.event(type, opt);
            } catch (err) {
                ERROR(err);
            }
        };

        Communicator.prototype.onMediaReady = function (opt) {
            this.media = opt;
            this.event(Event.MEDIA_READY, null);
        };

        Communicator.prototype.onMediaMessage = function (type, opt) {
            try  {
                this.event(type, opt);
            } catch (err) {
                ERROR(err);
            }
        };
        return Communicator;
    })();
    a3.Communicator = Communicator;
})(a3 || (a3 = {}));
//# sourceMappingURL=a3.js.map
