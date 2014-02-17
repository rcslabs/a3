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
            this._flashVars['logLevel'] = 'DEBUG';
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
//# sourceMappingURL=signaling.js.map
