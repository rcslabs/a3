function Phone(listener) {
	// audio:
	//   PCMA/8000
	//   PCMU/8000
	//   opus/48000
	//   ISAC/16000
	///
	var cc = {
			"userAgent" : "Chrome",
			//"audio" : ["ISAC/16000", "opus/48000" , "PCMA/8000", "PCMU/8000"],
			"audio" : [  "PCMA/8000"],
			"video" :  ["VP8/90000"],
			"profile" : "RTP/SAVPF",
			"ice": true,
			"rtcpMux" : true,
			"ssrcRequired" : true,
			"bundle": false
	};
	var vv = listener && listener.vv ? listener.vv : [true, true];

	function createHandler(listener, h) {
		return function() {
			if(listener && h in listener && typeof listener[h] === "function")
				try {
					listener[h].apply(listener, arguments);
				}catch(ex) {
					console.log("Exception in ", h ,  ex.message, ex.stack);
				}
		}
	}
	var beforeTransition    = createHandler(listener, "beforeTransition");
	var afterTransition     = createHandler(listener, "afterTransition");
	var onChatMessage       = createHandler(listener, "onChatMessage");
	var eventHandler        = createHandler(listener, "event");
	var mediaEventHandler   = createHandler(listener, "mediaEvent");



	//function SM(listener){
	//	this.eventSubscriptions = {}
	//}
	//SM.prototype = {
	//	event: function(/*...*/) {
	//		if(typeof arguments[0] === "function"){
	//			this.subscribe_on_event('*', arguments[0], Array.prototype.slice.call(arguments, 1));
	//		} else if(typeof arguments[0] === "string" && typeof arguments[1] === "function") {
	//			this.subscribe_on_event(arguments[0], arguments[1], Array.prototype.slice.call(arguments, 2));
	//		} else if(typeof arguments[0] === "string") {
	//			this.handleEvent.apply(this, [arguments[0]].concat(Array.prototype.slice.call(arguments, 1)) );
	//		}else {
	//			throw Error("StateMachine::event : wrong arguments")
	//		}
	//		return this;
	//	},
	//	sig : function() {
	//		return this;
	//	},
	//	subscribe_on_event: function(eventNames, callback, args) {
	//		eventNames.split(',').forEach(function(eventName){
	//			(this.eventSubscriptions[eventName] || (this.eventSubscriptions[eventName] = [])).push(callback);
	//		}.bind(this));
	//	},
	//	handleEvent : function(eventName, params) {
	//		this.eventSubscriptions[eventName] && this.eventSubscriptions[eventName].forEach(function(cb) { try{cb(params)}catch(err){console.log(err.message, err.stack)} })
	//	}
	//}



	var CallStateMachine = function(sessionId, signalling, media, listener) {
		var beforeTransition = createHandler(listener, "beforeTransition");
		var afterTransition  = createHandler(listener, "afterTransition");
		var onChatMessage    = createHandler(listener, "onChatMessage");
		var eventHandler     = createHandler(listener, "event");

		this.state = "START";
		this.sessionId = sessionId;
		this.signalling = signalling;
		this.media = media;
		this.callId = null;

		this.setState = function(newState, event, eventParams) {
			var fromState = this.state;
			beforeTransition(fromState, newState, event, eventParams)
			this.state = newState;
			afterTransition(fromState, newState, event, eventParams)
		}

		this.event = function(event, params) {
			switch(this.state) {
				case "START":
					if(event == "ui:start_call") {
						this.signalling.webcallRequest('START_CALL',
							{
								sessionId : this.sessionId,
								aUri: "1001",
								bUri: params.bUri,
								cc : cc,
								vv : params.vv,
								callTempId : params.callTempId
							});
						this.setState("CALL_STARTING", event, params);
					}else {
						this.unhandled(event, params);
					}
					break;

				case "CALL_STARTING":
					if (event === "CALL_STARTED") {
						this.setState("CALL_IN_PROGRESS", event, params);
					} else if (event === "CALL_FAILED") {
						this.setState("CALL_FINISHED");
						alert("CALL_FAILED, reason=" + params.reason)
					}else {
						this.unhandled(event, params);
					}
					break;

				case "CALL_IN_PROGRESS":
					if(event == "ui:hangup_call") {
						this.media.close();
						this.setState("CALL_FINISHED", event, params);
						this.signalling.webcallRequest("HANGUP_CALL",
						                               {
						                                   sessionId: this.sessionId,
						                                   callId: this.callId
						                               })
					} else if(event == "CALL_FINISHED" || event === "HANGUP_CALL") {
						this.media.close();
						this.setState("CALL_FINISHED", event, params);
					} else {
						this.unhandled(event, params);
					}
					break;

				case "CALL_FINISHED":
					this.unhandled(event, params);
			}
		}

		this.unhandled = function(event, params) {
			console.log("Unhandled event in CallSM. state=", this.state, "  event=", event, "  params=", params);
		}
	}


	var stateMachine = {
		state : "START",

		media      : null,
		signalling : null,
		sessionId  : null,
		aUri       : null,

		calls : {},
		addCall : function(params, call) {
			if("callId" in params) {
				this.calls[params.callId] = call
				call.callId = params.callId;
			} else {
				var callTempId = Math.random().toString();
				params.callTempId = callTempId;
				this.calls[callTempId] = call;
			}
		},
		getCallFromMessageParams: function(params) {
			var callId = params.callId;
			if(("callTempId" in params) && (params.callTempId in this.calls)) {
				var call = this.calls[params.callTempId];
				if("callId" in params) {                                     // replace temp id to callId
					delete this.calls[params.callTempId];
					this.addCall(params, call);
				}
				return call;
			}
			return (callId in this.calls) ? this.calls[callId] : null;
		},


		// actions

		setState : function(newState, event, eventParams) {
			var fromState = this.state;
			beforeTransition(fromState, newState, event, eventParams)
			this.state = newState;
			afterTransition(fromState, newState, event, eventParams)
		},


		event : function(event, params) {
			var result = null;
			eventHandler(this.state, event, params);
			switch(this.state) {
				case "START":
					if(event === "signalling-ready") {
						this.setState('LOGIN', event, params);
					} else {
						this.unhandled(event, params);
					}
					break;

				case "LOGIN":
					if(event === "ui:login") {
						stateMachine.aUri = params.phone;
						this.signalling.webcallRequest('START_SESSION', {username: params.phone, password: params.password});
						this.setState("LOGINING", event, params);
					} else {
						this.unhandled(event, params);
					}
					break;

				case "LOGINING":
					if(event === "SESSION_FAILED" || event === "SESSION_ERROR" || event === "SESSION_BROKEN") {
						this.setState('LOGIN', event, params);

					} else if(event === "SESSION_STARTED") {
						
						stateMachine.sessionId = params.sessionId;
						console.log("session started", stateMachine.sessionId);
						
						if(this.media) {
							this.setState('HARDWARE_SETTINGS_OK', event, params);
						} else {
							this.setState('INITIALIZING_MEDIA', event, params);
							this.media = new WebrtcMedia(this, vv);
							//
							// debug
							//
							//phone.media = this.media;
						}
					} else {
						this.unhandled(event, params);
					}
					break;

				case 'INITIALIZING_MEDIA' :
					if(event === "CONNECTION_BROKEN") {
						//setState ...
					} else if(event === 'media-ready') {
						this.setState('HARDWARE_SETTINGS', event, params);
						this.media.getHardwareState();
					} else {
						this.unhandled(event, params);
					}
					break

				case 'HARDWARE_SETTINGS' :
					if(event === "HardwareEvent.HARDWARE_STATE") {
						console.log("HARDWARE_STATE", params);
						this.setState('HARDWARE_SETTINGS_OK', event, params);
					} else {
						this.unhandled(event, params);
					}
					break;

				case 'HARDWARE_SETTINGS_OK':
					if(event === 'ui:start_call') {
						var call = new CallStateMachine(this.sessionId, this.signalling, this.media, arguments[2]);
						this.addCall(params, call);
						call.event('ui:start_call', params);
						result = call;

					} else if(event === "SDP_OFFER") {
						this.media.setOffererSDP(params);               // {sdp: params.sdp}

						if("callId" in params) {
							//
							// replace callTempId by CallId
							//
							var call = this.getCallFromMessageParams(params);
						}

					} else if(event === "media:local-sdp") {
						//var sdpAnswerMessage = this.sdpOfferMessage;
						//sdpAnswerMessage.sdp = params.sdp;
						//sdpAnswerMessage.sessionId = this.sessionId;
						//sdpAnswerMessage.callId = this.callId;
						this.signalling.webcallRequest("SDP_ANSWER", params);

					} else if(event === "CALL_STARTED"  || event === "CALL_STARTING"   ||
					          event === "HANGUP_CALL"   || event === "CALL_FINISHED" ||
						      event === "CALL_FAILED") {
						var call = this.getCallFromMessageParams(params);
						if(call)
							call.event(event, params);
						else
							console.log("Error: unknown callId ", params);
					} else if(event === 'CHAT_MESSAGE') {
						onChatMessage(params);
					} else {
						this.unhandled(event, params);
					}
					break;
			}
			return result;
		},

		unhandled : function(event, params) {
			console.log("Unhandled event in state ", this.state, "  event=", event, "  params=", params);
		},


		// inherited from SignallingListener and MediaListener
		swfReady : function(name) {
			if(name === "ru.rcslabs.webcall.SignalTransport") {
				this.event("signalling-ready");
			}
			if(name === "ru.rcslabs.webcall.MediaTransport") {
				this.event("media-ready");
			}
		},
		webcallMessageHandler : function(event, params) {
			console.log("webcallMessageHandler", event, params, this.state);
			this.event(event, params);
		},
		mediaHandler : function(event, params) {
			console.log("Media handler", event, params);
			mediaEventHandler(event, params);
			this.event(event, params);
		},



		// initialization
		init : function() {
			//this.signalling = new rcslabs.signalling.HttpSignalling(
			this.signalling = new CallSignalling(this);
		}
	};


	stateMachine.init();
	
	
	this.event = function(eventName, params){
		stateMachine.event(eventName, params);
	}

	this.sendChatBroadcast = function(params) {
		stateMachine.signalling.webcallRequest('CHAT_BROADCAST', params);
	}

	this.startCall = function(o, listener) {
		if(typeof o === "string") o = {bUri: o};
		if(!("vv" in o)) o.vv = [true, false];

		return stateMachine.event('ui:start_call', o, listener);
	}

	this.hangup = function(call) {
		call.event('ui:hangup_call');
	}
}

