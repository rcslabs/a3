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
            this._notifyListener('onCallStarting');
        };
        Call.prototype.onEnterStateRinging = function () {
            this._notifyListener('onCallRinging');
        };
        Call.prototype.onEnterStateProgress = function () {
            this._notifyListener('onCallStarted');
        };

        Call.prototype.onEnterStateFinished = function () {
            this.media.dispose();
            this._notifyListener('onCallFinished');
        };
        Call.prototype.onEnterStateFailed = function () {
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
                        this.media.checkHardware();
                    } else {
                        this._unhandledEvent(event, opt);
                    }
                    break;

                case State.SESSION_STARTED:
                    if (event === a3.CallEvent.CALL_STARTING || event === a3.CallEvent.CALL_STARTED || event === a3.CallEvent.SDP_OFFER) {
                        assert(call);
                        call.event(event, opt);
                    } else if (event === a3.CallEvent.CALL_FAILED || event === a3.CallEvent.CALL_FINISHED || event === a3.CallEvent.CALL_ERROR) {
                        //assert(call);
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
                        this.media.checkHardware();
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
//# sourceMappingURL=communicator.js.map
