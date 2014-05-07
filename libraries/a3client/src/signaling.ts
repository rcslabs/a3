module a3 {

	declare var io;
	declare var ERROR;
	declare var LOG;
	declare var swfobject;
	declare var assert;

	if(typeof LOG === "undefined") LOG = function() {};
	if(typeof ERROR === "undefined") ERROR = function() {};

	var MIN_FLASH_VERSION = '10.3.0';

	export class SessionEvent {
		public static SESSION_STARTED: string = "SESSION_STARTED";
		public static SESSION_STARTING: string = "SESSION_STARTING";
		public static SESSION_FAILED: string = "SESSION_FAILED";
		public static INCOMING_CALL: string =  "INCOMING_CALL";
	}

	export class CallEvent {
		public static CALL_STARTING: string ="CALL_STARTING";
		public static CALL_STARTED: string ="CALL_STARTED";
		public static CALL_FAILED: string ="CALL_FAILED";
		public static CALL_FINISHED: string ="CALL_FINISHED";
		public static CALL_TRANSFERED: string ="CALL_TRANSFERED";
		public static CALL_ERROR: string = "CALL_ERROR";
		public static SDP_OFFER: string = "SDP_OFFER";
		public static SDP_ANSWER: string = "SDP_ANSWER";
	}

	export interface ISignalingListener {
		onSignalingReady(o: ISignaling);
		onSignalingFailed(o: ISignaling);
		onSignalingConnected(o: ISignaling);
		onSignalingConnectionFailed(o: ISignaling);
		onSignalingMessage(type: string, opt: any);
	}

	export interface ISignaling {
		start();
		addEndpoint(url: string);
		connect();
		request(type: string, opt: Object);
		setService(value:string);
		setClientInfo(prop: string, value: string);
		open(username: string, password: string, challenge: string, code: string);
		startCall(bUri: string, cc: Object, vv: boolean[]);
		hangup(callId: string);
		sdpAnswer(callId: string, pointId: string, sdp: any);
		dtmf(callId: string, dtmf: string);
	}

	export class SioSignaling implements ISignaling {

		private _url: string = null;
		private _service: string = null;
		private _socket: any = null;
		private _listener:ISignalingListener;
		private _intv = 0; // interval for connection setTimeout

		public sessionId: string = "";

		constructor(listener: ISignalingListener) {
			this._listener = listener;
		}

		start() {
			// this instance ready immediately
			this._notifyListener('onSignalingReady', this);
		}

		setClientInfo(prop: string, value: string) {
			//
		}

		addEndpoint(url:string) {
			this._url = url;
		}

		setService(value:string) {
			this._service = value;
		}

		connect() {
			if(this._intv){ clearTimeout(this._intv); }
			if(this._socket != null){ /* TODO: remove all listeners correctly */ }
			this._socket = io.connect(this._url);

			this._intv = setTimeout(() => {
				this._socket = null;
				this._notifyListener('onSignalingConnectionFailed', this);
			}, 3000);

			this._socket.on('connect', () => {
				clearTimeout(this._intv);
				this._notifyListener('onSignalingConnected', this);
			});

			this._socket.on('error', () => {
				clearTimeout(this._intv);
				this._notifyListener('onSignalingConnectionFailed', this);
			});

			this._socket.on('disconnect', () => {
				clearTimeout(this._intv);
				this._notifyListener('onSignalingConnectionFailed', this);
			});

			this._socket.on('connect_failed', () => {
				clearTimeout(this._intv);
				this._notifyListener('onSignalingConnectionFailed', this);
			});

			this._socket.on('reconnect_failed', () => {
				clearTimeout(this._intv);
				this._notifyListener('onSignalingConnectionFailed', this);
			});

			this._socket.on('message', (message) => {
				var type: string = message.type;
				delete message.type;

				if(type === SessionEvent.SESSION_STARTED || type === SessionEvent.SESSION_STARTING) {
					this.sessionId = message.sessionId;
				}
				this._notifyListener('onSignalingMessage', type, message);
			});
		}

		open(username: string, password: string, challenge: string, code: string) {
			this.request("START_SESSION", {username: username, password: password});
		}

		startCall(bUri: string, cc: Object, vv: boolean[]) {
			this.request("START_CALL", {
				sessionId : this.sessionId,
				aUri: "",
				bUri: bUri,
				cc : cc,
				vv : vv
			});
		}

		dtmf(callId, dtmf) {
			this.request("SEND_DTMF", {
				sessionId: this.sessionId,
				callId: callId,
				dtmf: dtmf
			});
		}

		hangup(callId: string) {
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
		}

		sdpAnswer(callId: string, pointId: string, sdp: any) {
			this.request("SDP_ANSWER", {
				sessionId: this.sessionId,
				callId: callId,
				pointId: pointId,
				sdp: sdp
			});
		}

		request(type: string, opt: Object) {
			if(!opt) opt = {};
			opt["type"] = type;
			opt["service"] = this._service;

			// added 'typz' feature - quick fix
			switch (type){
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

			if(type != "START_SESSION") opt["sessionId"] = this.sessionId;
			LOG("SENDING TO SOCKET.IO: ", type, opt);
			this._socket.emit('message', opt);
		}

		_notifyListener(callback: string, ...args: any[]) {
			if(this._listener == null) return;
			if(typeof this._listener[callback] !== 'function') return;
			this._listener[callback].apply(this._listener, Array.prototype.slice.call(arguments, 1));
		}
	}

	export class FlashSignaling implements ISignaling {

		public sessionId: string = "";
		private _endpoints:string[] = [];
		private _swf: any = null;
		private _service: string = null;

		constructor(private _listener: ISignalingListener, private _container:string, private _flashVars: any) {
			this._flashVars = this._flashVars || {};
			var readyCallback = '__'+Math.round(Math.random()*Math.pow(10,16));
			this._flashVars['cbReady'] = readyCallback;
			window[readyCallback] = () => { this._listener.onSignalingReady(this); }
			var signalingCallback = '__'+Math.round(Math.random()*Math.pow(10,16));
			this._flashVars['cbSignaling'] = signalingCallback;
			window[signalingCallback] = (e) => { this.onSignalingMessage(e.type, e); }
		}

		start() {
			// Note: value "microphoneVolume" should be named micVolume
			swfobject.embedSWF(
				"RTMP2JS.swf", this._container, "1", "1", MIN_FLASH_VERSION, null,
				this._flashVars || {},
				{
					'allowScriptAccess': 'always',
					'wmode': 'transparent'
				},
				{ id: "a3-swf-signaling", name: "a3-swf-signaling" },
				(e) => {
					if (!e.success) {
						this._listener.onSignalingFailed(this);
					} else {
						this._swf = e.ref;
					}
				});
		}

		addEndpoint(url:string) {
			this._endpoints.push(url);
		}

		setService(value:string) { }

		connect() {
			// add all enpoints once
			while(this._endpoints.length){ this._swf.addEndpoint(this._endpoints.shift())}
			this._swf.connect();
		}

		close() {
			this._swf.close();
		}

		setClientInfo(prop, value) {
			this._swf.setClientInfo(prop, value);
		}

		request(type: string, opt: Object) {
			if(!opt) opt = {};
			opt["type"] = type;
			opt["service"] = this._service;
			if(type != "START_SESSION") opt["sessionId"] = this.sessionId;
			LOG("SENDING TO Flash: ", type, opt);
			this._swf.notifyMessage(opt);
		}

		_createOfferFromFlashSignaling(data:any) {
			var rtmpUrls = {
				playUrlVideo: data.playUrlVideo,
				playUrlVoice: data.playUrlVoice,
				publishUrlVideo: data.publishUrlVideo,
				publishUrlVoice: data.publishUrlVoice
			};
			return rtmpUrls;
		}

		// ISignalingListener::onSignalingMessage
		onSignalingMessage(type: string, data: any) {
			switch(type) {
				case "CONNECTING": // nothing happens
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
					if(null != offerSdp){
						this._listener.onSignalingMessage("SDP_OFFER", {
							pointId: data.callId,
							callId: data.callId,
							sdp: offerSdp
						});
					}
					this._listener.onSignalingMessage("CALL_STARTED", {callId: data.callId});
					break;

				default:
					this._listener.onSignalingMessage(type, data);
			}
		}

		open(username: string, password: string, challenge: string, code: string) {
			this.request("START_SESSION", {username: username, password: password});
		}

		startCall(bUri: string, cc: Object, vv: boolean[]) {
			this.request("START_CALL", {
				aUri: "",
				bUri: bUri,
				// ignore cc
				vv : vv
			});
		}

		hangup(callId: string) {
			this.request("HANGUP_CALL", {
				callId: callId
			});
		}

		dtmf(callId, dtmf) {
			this.request("SEND_DTMF_SIGNAL", {
				callId : callId,
				dtmfString : dtmf
			});
		}

		sdpAnswer(callId: string, pointId: string, sdp: any) {
			// Do nothing
		}

		accept(callId, av_params) {
			// TODO: this._swf.webcallRequest("accept", {callId: callId, av_params: av_params});
		}

		decline(callId) {
			// TODO: this._swf.webcallRequest("decline", {callId: callId});
		}
	}



	/* TODO: add
		<<interfaces>>:
			ITransport: with methods start, request, addListener
			ISignaling: with methods open, dtmf, startCall, hangup, addListener

		<<classes>>
			SignalingImpl implements ISignaling : implement signaling API with AleNa-3
			Webcall2SignalingImpl implements ISignaling : implements signaling API with Webcall-2

			SioTransport implements ITransport : implements socket.io
			FlashTransport implements ITransport : implement with RTMP2JS

			SioSignaling extends SignalingImpl, aggregates transport: ITransport = new SioTransport(...)
			Webcall2FlashSignaling extends Webcall2SignalingImpl, aggregates transport: ITransport = new FlashTransport
	 */
}
