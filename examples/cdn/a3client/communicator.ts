/// <reference path="signaling.ts" />
/// <reference path="media.ts" />

module a3 {

	declare var assert;
	declare var ERROR;
	declare var WARN;
	declare var LOG;


	if(typeof assert !== "function") assert = function() {};
	if(typeof ERROR !== "function") ERROR = function() { throw("Error")};
	if(typeof WARN !== "function") WARN = function() {};
	if(typeof LOG !== "function") LOG = function() {};

	var STARTING_TIMEOUT = 10000;

	export interface ICommunicatorFactory {
		createMedia(listener: IMediaListener): IMedia;
		createSignaling(listener: ISignalingListener): ISignaling;
	}

	export interface ICall {
		setListener(listener: ICallListener);
	}

	export interface ICallListener{
		onIncomingCall(call: Call);
		onCallStarting(call: Call);
		onCallStarted(call: Call);
		onCallFinished(call: Call);
		onCallFailed(call: Call);
		onDurationChanged(call: Call, duration: number);
	}

	// self-listenable
	export interface ICommunicator extends ICommunicatorListener {
		connect();
		setSoundVolume(value:number);
	}

	export interface ICommunicatorListener {
		onCommunicatorStarting();
		onCommunicatorStarted();
		onCommunicatorFailed();

		onConnecting();
		onConnected();
		onConnectionFailed();

		onCheckHardwareSettings();
		onCheckHardwareReady();
		onCheckHardwareFailed();
		onSoundVolumeChanged(value:number);
		onSessionStarting();
		onSessionStarted();
		onSessionFailed();
	}

	export class Event {
		//
		// a custom set of user-events
		//
		//
		public static START: string = "e:START";
		public static SIGNALING_READY: string = "e:SIGNALING_READY";
		public static SIGNALING_FAILED: string = "e:SIGNALING_FAILED";
		public static MEDIA_READY: string = "e:MEDIA_READY";
		public static MEDIA_FAILED: string = "e:MEDIA_FAILED";
		public static START_TIMEOUT: string = "e:START_TIMEOUT";
		public static CONNECT: string = "e:CONNECT";
		public static CONNECTED: string = "e:CONNECTED";
		public static CONNECTION_FAILED: string = "e:CONNECTION_FAILED";
		public static HARDWARE_STATE_CHANGED: string = "HardwareEvent.HARDWARE_STATE";
		public static SOUND_VOLUME_CHANGED: string = "HardwareEvent.SOUND_VOLUME_CHANGED";
		public static TRANSFER_FAILED: string = "TRANSFER_FAILED";
		public static CANCEL: string = "CANCEL";

		public static START_INCOMING: string = "e:START_INCOMING";
		public static ACCEPT_CALL: string = "e:ACCEPT_CALL";
		public static DECLINE_CALL: string = "e:DECLINE_CALL";
	}



	export class State {
		public static STARTING: string = "s:STARTING";
		public static SIGNALING_READY: string = "s:SIGNALING_READY";
		public static MEDIA_READY: string = "s:MEDIA_READY";
		public static STARTED: string = "s:STARTED";
		public static FAILED: string = "s:FAILED";
		//public static HARDWARE_SETTINGS: string = "s:HARDWARE_SETTINGS";
		public static CONNECTING: string = "s:CONNECTING";
		public static CONNECTED: string = "s:CONNECTED";
		public static CONNECTION_FAILED: string = "s:CONNECTION_FAILED";
		public static SESSION_STARTING: string = "s:SESSION_STARTING";
		public static SESSION_STARTED: string = "s:SESSION_STARTED";
		public static SESSION_FAILED: string = "s:SESSION_FAILED";
		public static DISCONNECTED: string = "s:DISCONNECTED";
	}
	export class CallState {
		public static STARTING: string = "Call:STARTING";
		public static RINGING: string = "Call:RINGING";
		public static PROGRESS: string = "Call:PROGRESS";
		public static FINISHED: string = "Call:FINISHED";
		public static FAILED: string = "Call:FAILED";
	}

	export class CallType {
		public static AUDIO: string = "audio";
		public static VIDEO: string = "video";
		public static BOTH: string = "audio-video";
	}

	export class Call implements ICall{
		private _uid: string = null;
		private _state: string = null;
		private _duration: number = 0;
		private _durationTimerHandle: number = null;
		private _listener: ICallListener = null;

		constructor(private _vv, private media: IMedia, private signaling: ISignaling) {
		}

		setListener(listener: ICallListener) {
			this._listener = listener;
		}

		getId(): string { return this._uid; }
		setId(id) { this._uid = id;   }
		isAudio(): boolean { return this._vv[0] === true; }
		isVideo(): boolean { return this._vv[1] === true; }

		accept() { this.event(Event.ACCEPT_CALL, null); }
		decline() { this.event(Event.DECLINE_CALL, null); }
		start() { this.event(Event.START, null); }
		startIncoming() { this.event(Event.START_INCOMING, null); }

		sendDTMF(dtmf: string) {
			this.signaling.dtmf(this._uid, dtmf);
		}
		getState(): string { return this._state; }

		hangup() {
			assert(this._uid, "Call::hangup: self._uid === null");
			this.signaling.hangup(this._uid);
		}
		remove() {
			LOG("Remove CALL");
		}
		setMicrophoneVolume(value) { this.media.setMicrophoneVolume(value); }
		setSoundVolume(value) { this.media.setSoundVolume(value); }

		onEnterStateStarting() {
			this.media.playRBT();
			this._notifyListener('onCallStarting');
		}

		onEnterStateRinging() { this._notifyListener('onCallRinging'); } // incoming call starting
		onEnterStateProgress() {
			this.media.stopRBT();
			this._notifyListener('onCallStarted');
		}

		onEnterStateFinished() {
			this.media.stopRBT();
			this.media.dispose();
			this._notifyListener('onCallFinished');
		}
		onEnterStateFailed() {
			this.media.stopRBT();
			this.media.dispose();
			this._notifyListener('onCallFailed');
		}

		_notifyListener(callback: string) {
			if(this._listener == null) return;
			if(typeof this._listener[callback] !== 'function') return;
			this._listener[callback](this);
		}

		_setState(newState) {
			if(this._state !== newState) {
				this._state = newState;
				switch(this._state) {
					case CallState.RINGING:  this.onEnterStateRinging();  break;
					case CallState.STARTING: this.onEnterStateStarting(); break;
					case CallState.PROGRESS: this.onEnterStateProgress(); break;
					case CallState.FINISHED: this.onEnterStateFinished(); break;
					case CallState.FAILED:   this.onEnterStateFailed();   break;
				}
			}
		}
		_unhandledEvent(event, opt) {
			WARN("Unhandled event", event, " in state=", this._state);
		}
		_startTimer() {
			if(this._durationTimerHandle)
				return;
			var startTime: number = new Date().valueOf();
			this._durationTimerHandle = window.setInterval(()=> {
				var time: number = new Date().valueOf();
				var duration: number = Math.round((time - startTime) / 1000);
				if(this._duration !== duration) {
					this._duration = duration;
					this._listener.onDurationChanged(this, this._duration);
				}
			}, 250);
		}
		_stopTimer() {
			if(this._durationTimerHandle) {
				window.clearInterval(this._durationTimerHandle);
				this._durationTimerHandle = null;
			}
		}
		event(event: string, opt: any) {
			LOG("SM-CALL: state=", this._state, " event=", event, "opt=", opt);

			switch(this._state) {
				case null:
					if(event === Event.START) {
						this._setState(CallState.STARTING);
					} else if(event === Event.START_INCOMING) {
						this._setState(CallState.RINGING)
					}else {
						this._unhandledEvent(event, opt);
					}
					break;

				case CallState.STARTING:
					if(event === CallEvent.CALL_STARTED) {
						this._startTimer();
						this._setState(CallState.PROGRESS);
					} else if(event === CallEvent.CALL_STARTING) {
						// OK
					} else if(event === CallEvent.SDP_OFFER) {
						this.__onSdpOffer(opt);
					} else if(event === CallEvent.CALL_FAILED || event === CallEvent.CALL_ERROR) {
						this._setState(CallState.FAILED);
					} else if(event === CallEvent.CALL_FINISHED) {
						this._setState(CallState.FINISHED);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case CallState.RINGING:
					if(event === CallEvent.CALL_STARTED) {
						this._startTimer();
						this._setState(CallState.PROGRESS);
					} else if(event === Event.ACCEPT_CALL) {
						this._startTimer();
						this._setState(CallState.PROGRESS);
					} else if(event === Event.DECLINE_CALL) {
						this._setState(CallState.FINISHED);
					} else if(event === CallEvent.CALL_FAILED || event === CallEvent.CALL_ERROR) {
						this._setState(CallState.FAILED);
					} else if(event === CallEvent.CALL_FINISHED) {
						this._setState(CallState.FINISHED);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case CallState.PROGRESS:
					if(event === CallEvent.CALL_FINISHED) {
						this._stopTimer();
						this._setState(CallState.FINISHED);
					} else if(event === CallEvent.CALL_FAILED || event === CallEvent.CALL_ERROR) {
						this._setState(CallState.FAILED);
					} else
						this._unhandledEvent(event, opt);
					break;


				case CallState.FINISHED:
					this._unhandledEvent(event, opt);
					break;
			}
		}
		__onSdpOffer(opt) {
			var pointId = opt.pointId;
			var callId = opt.callId;
			var offerSdp = opt.sdp;
			this.media.setOfferSdp(callId, pointId, offerSdp);
		}
	}

	export class Communicator implements ICommunicator, ICallListener, IMediaListener, ISignalingListener {

		public factory:ICommunicatorFactory;
		public sessionId:string = null;
		public calls: Call[] = [];
		public signaling: ISignaling = null;
		public media: IMedia = null;

		private _startingTimeout = 0;
		private _state: string = null;
		private _microphoneState: string = HardwareState.DISABLED;
		private _cameraState: string = HardwareState.DISABLED;

		constructor() {
		}

		setFactory(factory: ICommunicatorFactory) {
			this.factory = factory;
		}

		// communicator subject
		onCommunicatorStarting() {}
		onCommunicatorStarted() {}
		onCommunicatorFailed() {}

		// signaling connection
		onConnecting() {}
		onConnected() {}
		onConnectionFailed() {}

		// hardware
		onCheckHardwareSettings() {}
		onCheckHardwareReady() {}
		onCheckHardwareFailed() {}
		onSoundVolumeChanged(value:number) {}

		// session
		onSessionStarting() {}
		onSessionStarted() {}
		onSessionFailed() {}

		// calls
		onCallStarting(call:a3.Call) {}
		onCallStarted(call:a3.Call) {}
		onCallFinished(call:a3.Call) {}
		onCallFailed(call:a3.Call) {}
		onIncomingCall(call:a3.Call) {}
		onDurationChanged(call:a3.Call, duration:number) {}


		start() {
			if(this._state === null) {
				this._startingTimeout = setTimeout(() => {
					this.event(Event.START_TIMEOUT, null)
				}, STARTING_TIMEOUT);

				this._setState(State.STARTING);

				this.media = this.factory.createMedia(this);
				if(this.media == null){
					this.event(Event.MEDIA_FAILED, null);
				}else{
				this.media.start();
				}

				this.signaling = this.factory.createSignaling(this);
				if(this.signaling == null){
					this.event(Event.SIGNALING_FAILED, null);
				}else{
				this.signaling.start();
				}
			} else {
				throw new Error("Method `start` in wrong state");
			}
		}

		connect() {
			this.event(Event.CONNECT, null);
			this.signaling.connect();
		}

		open(phone, password, challenge, code) {
			this.event(a3.SessionEvent.SESSION_STARTING, null);
			this.signaling.open(phone, password, challenge, code);
		}

		close() {}

		startCall(destination, vv) {
			if(this._state !== State.SESSION_STARTED) {
				throw new Error("Start the call in wrong state " + this._state);
			}

			var call: Call = this._createCallInstance(vv);
			call.setListener(this);
			this._addCall(call);
			call.start();
			var cc = this.media.getCc();
			this.signaling.startCall(destination, cc, vv);
		}

		setSoundVolume(value:number){
			this.media.setSoundVolume(value);
		}

		getMicrophoneState() {return this._microphoneState;}

		getCameraState() { return this._cameraState; }

		_createCallInstance(vv: boolean[]): Call {
			return new Call(vv, this.media, this.signaling);
		}

		_getCallById(id) {
			for(var i = 0; i < this.calls.length; i++)
				if(this.calls[i].getId() === id)
					return this.calls[i];
			return null;
		}

		_addCall(call) {
			this.calls.push(call);
		}

		_removeCall(call) {
			for(var i = 0; i < this.calls.length; i++)
				if(call === this.calls[i]) {
					this.calls.splice(i, 1);
					call.remove();
					return;
				}
			assert(0);
		}

		_setState(newState) {
			if(this._state !== newState) {
				this._state = newState;
				switch(this._state) {
					case State.STARTING:            this.onCommunicatorStarting();    break;
					case State.STARTED:             this.onCommunicatorStarted();     break;
					case State.FAILED:              this.onCommunicatorFailed();      break;
					case State.CONNECTING:          this.onConnecting();              break;
					case State.CONNECTED:           this.onConnected();               break;
					case State.CONNECTION_FAILED:   this.onConnectionFailed();        break;
					case State.SESSION_STARTING:    this.onSessionStarting();         break;
					case State.SESSION_STARTED:     this.onSessionStarted();          break;
					case State.SESSION_FAILED:      this.onSessionFailed();           break;
					case State.DISCONNECTED:        this.onConnectionFailed();        break;
				}
			}
		}

		_setHardwareState(microphoneState, cameraState) {
			this._microphoneState = microphoneState;
			this._cameraState = cameraState;
		}

		getState() { return this._state; }

		_unhandledEvent(event: string, opt: any) {
			WARN("Unhandled event", event, " in state=", this._state);
		}

		event(event: string, opt: any) {
			LOG("SM: state=", this._state, " event=", event, "opt=", opt);

			if(event == Event.SOUND_VOLUME_CHANGED){
				this.onSoundVolumeChanged(opt.data);
				return;
			}

			var call: Call = null;
			if(opt && opt.callId) {
				call = this._getCallById(opt.callId) || this._getCallById(null);
				if(call && !call.getId()) {
					LOG("Setting call uid=" + opt.callId);
					call.setId(opt.callId);
				}
			}

			switch(this._state) {
				case State.STARTING:
					if(event === Event.SIGNALING_READY) {
						this._setState(State.SIGNALING_READY);
					} else if(event === Event.MEDIA_READY) {
						this._setState(State.MEDIA_READY);
					} else if(event === Event.SIGNALING_FAILED || event === Event.MEDIA_FAILED || event === Event.START_TIMEOUT) {
						this._setState(State.FAILED);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case State.SIGNALING_READY:
					if(event === Event.MEDIA_READY) {
						clearInterval(this._startingTimeout);
						this._setState(State.STARTED);
					} else if(event === Event.MEDIA_FAILED || event === Event.START_TIMEOUT) {
						this._setState(State.FAILED);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case State.MEDIA_READY:
					if(event === Event.SIGNALING_READY) {
						clearInterval(this._startingTimeout);
						this._setState(State.STARTED);
					} else if(event === Event.SIGNALING_FAILED || event === Event.START_TIMEOUT) {
						this._setState(State.FAILED);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case State.STARTED:
					clearInterval(this._startingTimeout);
					// continue
				case State.CONNECTION_FAILED:
					if (event === Event.CONNECT) {
						this._setState(State.CONNECTING);
					} else
						this._unhandledEvent(event, opt);
					break;

				case State.CONNECTING:
					if(event === Event.CONNECTED) {
						this._setState(State.CONNECTED);
					} else if(event === Event.CONNECTION_FAILED){
						this._setState(State.CONNECTION_FAILED);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case State.CONNECTED:
					if(event === Event.CONNECTION_FAILED){
						this._setState(State.CONNECTION_FAILED);
					} else if(event === SessionEvent.SESSION_STARTING) {
						this._setState(State.SESSION_STARTING);
					} else if(event === SessionEvent.SESSION_FAILED) {
						this._setState(State.SESSION_FAILED);
					} else if (event === Event.HARDWARE_STATE_CHANGED) {
						this._onHardwareStateChanged(opt);
					} else if(event === Event.MEDIA_READY) { // Flash element is reloaded
						this.media.checkHardware([true, true]);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case State.SESSION_STARTED:
					if(event === CallEvent.CALL_STARTING || event === CallEvent.CALL_STARTED || event === CallEvent.SDP_OFFER) {
						assert(call);
						call.event(event, opt);
					} else if(event === CallEvent.CALL_FAILED || event === CallEvent.CALL_FINISHED || event === CallEvent.CALL_ERROR) {
						//assert(call);
						if(call) {
							call.event(event, opt);
							this._removeCall(call);
							LOG("Calls count = ", this.calls.length);
						} else {
							LOG("Warning: Trying to remove unexistent call")
						}
					} else if(event === CallEvent.SDP_ANSWER) {
						assert(call);
						this.signaling.sdpAnswer(opt.callId, opt.pointId, opt.sdp);
					} else if(event === SessionEvent.INCOMING_CALL) {
						var call: Call = this._createCallInstance(opt.vv);
						this._addCall(call);
						call.startIncoming();
						this.onIncomingCall(call);
					} else if (event === Event.HARDWARE_STATE_CHANGED) {
						this._onHardwareStateChanged(opt);
					} else if(event === Event.MEDIA_READY) { // Flash element is reloaded
						this.media.checkHardware([true, true]);
					} else {
						this._unhandledEvent(event, opt);
					}
					break;

				case State.SESSION_STARTING:
					if(event === Event.CONNECTION_FAILED){
						this._setState(State.CONNECTION_FAILED);
					} else if (event === SessionEvent.SESSION_STARTED) {
						this._setState(State.SESSION_STARTED);
					} else if (event === SessionEvent.SESSION_FAILED) {
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
		}

		_onHardwareStateChanged(opt:any) {
			if ( opt.data.microphone.state === HardwareState.ENABLED) {
				this._setHardwareState(opt.data.microphone.state, opt.data.camera.state);
				if(opt.data.userDefined){
					// user declined microphone
					this.onCheckHardwareReady();
				} else {
					this.onCheckHardwareSettings();
				}
			} else if(opt.data.microphone.state === HardwareState.DISABLED) {
				if(opt.data.userDefined){
					// user declined microphone
					this.onCheckHardwareFailed();
				} else {
					this.onCheckHardwareSettings();
				}
			}
		}

		onSignalingReady(o: ISignaling) {
			this.signaling = o;
			this.event(Event.SIGNALING_READY, o);
		}

		onSignalingFailed(o:a3.ISignaling) {
			this.event(Event.SIGNALING_FAILED, o);
		}

		onSignalingConnected(o:a3.ISignaling) {
			this.event(Event.CONNECTED, o);
		}

		onSignalingConnectionFailed(o:a3.ISignaling) {
			this.signaling = o;
			this.event(Event.CONNECTION_FAILED, o);
		}

		onSignalingMessage(type: string, opt: any) {
			try {
				this.event(type, opt);
			}catch(err) {
				ERROR(err);
			}
		}

		onMediaReady(opt) {
			this.media = opt;
			this.event(Event.MEDIA_READY, null);
		}

		onMediaMessage(type, opt) {
			try {
				this.event(type, opt);
			}catch(err) {
				ERROR(err);
			}
		}
	} // Communicator

}
