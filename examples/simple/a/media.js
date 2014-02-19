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

    var MIN_FLASH_VERSION = '10.3.0';
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
    var RTCSessionDescription = window.RTCSessionDescription;
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
            this._pubVoice = undefined;
            this._pubVideo = undefined;
            this._subVoice = undefined;
            this._subVideo = undefined;
            this._swf = null;
            this._flashVars = this._flashVars || {};
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
            this._flashVars['logLevel'] = 'ALL';
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
                audio: ["PCMA/8000"],
                video: ["H264/90000"],
                "profile": "RTMP"
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

        FlashMedia.prototype.checkHardware = function () {
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
            if (typeof offerSdp === "string") {
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
                                break;
                            case 1:
                                if (line.match("^a=rtmp-sub:(.+)$"))
                                    this._pubVoice = RegExp.$1;
                                if (line.match("^a=rtmp-pub:(.+)$"))
                                    this._subVoice = RegExp.$1;
                                break;
                            case 2:
                                if (line.match("^a=rtmp-sub:(.+)$"))
                                    this._pubVideo = RegExp.$1;
                                if (line.match("^a=rtmp-pub:(.+)$"))
                                    this._subVideo = RegExp.$1;
                                break;
                        }
                    }
                }
            } else {
                this._pubVoice = offerSdp.publishUrlVoice;
                this._pubVideo = offerSdp.publishUrlVideo;
                this._subVoice = offerSdp.playUrlVoice;
                this._subVideo = offerSdp.playUrlVideo;
            }

            this._swf.publish(this._pubVoice, this._pubVideo);
            this._swf.subscribe(this._subVoice, this._subVideo);

            if (typeof offerSdp === "string") {
                this._listener.onMediaMessage(MediaEvent.SDP_ANSWER, {
                    callId: callId,
                    pointId: pointId,
                    sdp: offerSdp
                });
            }
        };

        FlashMedia.prototype.dispose = function () {
            this._swf.unpublish();
            this._swf.unsubscribe();
        };

        FlashMedia.prototype.playDtmf = function (dtmf) {
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
            this.getLocalAudioTrack().enabled = !value; //(value ? 0 : this._micVolume/100);
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
            this.__pcVideo = null;
            this.__pc = null;
        };

        WebrtcMedia.prototype.checkHardware = function () {
            var _this = this;
            this._notify("HardwareEvent.HARDWARE_STATE", { data: {
                    microphone: { state: HardwareState.DISABLED },
                    camera: { state: HardwareState.DISABLED },
                    userDefined: false
                } });
            getUserMedia({
                audio: true,
                video: {
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
                }
            }, function (stream) {
                var audioTracks = stream.getAudioTracks();
                var videoTracks = stream.getVideoTracks();
                _this._localStream = stream;
                _this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", { data: {
                        microphone: {
                            state: audioTracks.length ? HardwareState.ENABLED : HardwareState.ABSENT,
                            wtfName: audioTracks.length ? audioTracks[0].label : ""
                        },
                        camera: {
                            state: videoTracks.length ? HardwareState.ENABLED : HardwareState.ABSENT,
                            wtfName: videoTracks.length ? videoTracks[0].label : ""
                        },
                        userDefined: true
                    } });
            }, function (err) {
                LOG("error getting mediastream", err);
                _this._localStream = undefined;
                _this._notify("HardwareEvent.HARDWARE_STATE", { data: {
                        microphone: { state: HardwareState.DISABLED },
                        camera: { state: HardwareState.DISABLED },
                        userDefined: true
                    } });
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
            volume.gain.value = 0.5;
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
//# sourceMappingURL=media.js.map
