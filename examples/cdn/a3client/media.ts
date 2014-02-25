/*
a3client Media

 * webrtc media module
 * DTMF beeper


TODO:
create Sdp parser and builer

 */

module a3 {

	declare var ERROR;
	declare var LOG;
	declare var swfobject;
	declare var assert;

	if(typeof LOG === "undefined") LOG = function() {};
	if(typeof ERROR === "undefined") ERROR = function() {};

	var MIN_FLASH_VERSION = '10.3.0';
	var DTMF_DURATION: number = 140;
	var DTMF_INTERVAL: number = 200;
	var VIDEO_WIDTH: number = 352;
	var VIDEO_HEIGHT: number = 288;
	var VIDEO_FRAMERATE: number = 15;

	export class MediaEvent {
		public static SDP_ANSWER = "SDP_ANSWER";
	}

	export class HardwareState {
		public static DISABLED: string = "disabled";
		public static ABSENT: string = "absent";
		public static ENABLED: string = "enabled";
	}

	export interface IMediaListener {
		onMediaReady(o: IMedia);
		onMediaMessage(type: string, opt: any);
	}

	export interface IMedia {
		start();
		getCc(): any;
		checkHardware();
		setMicrophoneVolume(volume: number);
		setSoundVolume(volume: number);
		muteMicrophone(value: boolean);
		muteSound(value: boolean);
		setOfferSdp(callId: string, pointId: string, offerSdp: any);
		playDtmf(dtmf: string);
		dispose();
	}


	//
	// adapter.js
	//
	var RTCPeerConnection: any = null;
	var RTCSessionDescription: any = (<any>window).RTCSessionDescription;
	var getUserMedia: any = null;
	var attachMediaStream: any = null;

	if (navigator["mozGetUserMedia"]) {
		RTCPeerConnection = window["mozRTCPeerConnection"];
		getUserMedia = navigator["mozGetUserMedia"].bind(navigator);
		attachMediaStream = function(element, stream) {
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
		attachMediaStream = function(element, stream) {
			element.src = window["webkitURL"].createObjectURL(stream);
		};
	} else {
		LOG("Browser does not appear to be WebRTC-capable");
	}

	export class FlashMedia implements IMedia
	{
		private _pubVoice: any = undefined;
		private _pubVideo: any = undefined;
		private _subVoice: any = undefined;
		private _subVideo: any = undefined;
		private _swf: any = null;

		constructor(private _listener: IMediaListener, private _container:HTMLElement, private _flashVars: any) {
			var flashContainer = document.createElement('div');
			flashContainer.id = 'this-div-will-replaced-by-swf';
			this._container.appendChild(flashContainer);
			this._container = flashContainer;

			this._flashVars = this._flashVars || {};
			var readyCallback = '__'+Math.round(Math.random()*Math.pow(10,16));
			this._flashVars['cbReady'] = readyCallback;
			window[readyCallback] = () => { this._listener.onMediaReady(this); }
			var mediaCallback = '__'+Math.round(Math.random()*Math.pow(10,16));
			this._flashVars['cbMedia'] = mediaCallback;
			window[mediaCallback] = (e) => { this._listener.onMediaMessage(e.type, e); }
		}

		start() {
			// Note: value "microphoneVolume" should be named micVolume
			swfobject.embedSWF(
				"MEDIA2JS.swf", this._container.id, "100%", "100%", MIN_FLASH_VERSION, null,
				this._flashVars,
				{
					'allowScriptAccess': 'always' //, 'wmode': 'transparent'
				},
				{ id: "a3-swf-media", name: "a3-swf-media" },
				(e) => {
					if (!e.success) {
						this._listener.onMediaReady(null);
					} else {
						this._swf = e.ref;
					}
				});
		}

		getCc(): any {
			return {
				userAgent: "FlashPlayer",
				audio: ["PCMA/8000"],
				video: ["H264/90000"],
				"profile" : "RTMP"
			};
		}

		setMicrophoneVolume(value) {
			this._swf.microphoneVolume(value);
		}

		setSoundVolume(value) {
			this._swf.soundVolume(value);
		}

		muteMicrophone(value:boolean) {
			this._swf.muteMicrophone(value);
		}

		muteSound(value:boolean) {
			this._swf.muteSound(value);
		}

		checkHardware() {
			this._swf.checkHardware();
		}

		setOfferSdp(callId: string, pointId: string, offerSdp: any) {
			// currently offerSdp in form
			//{
			//  playUrlVideo: [...]
			//  playUrlVoice: [...]
			//  publishUrlVideo: [...]
			//  publishUrlVoice: [...]
			//}
			if(typeof offerSdp === "string") {
				var lines: string[] = offerSdp.split("\r\n");
				var state = 0;
				for(var i=0; i<lines.length; i++) {
					var line: string = lines[i];
					if(line.match(/^m=audio/)) state = 1;
					if(line.match(/^m=video/)) state = 2;
					if(line) {
						switch(state) {
							case 0: break;
							case 1:
								if(line.match("^a=rtmp-sub:(.+)$")) this._pubVoice = RegExp.$1;
								if(line.match("^a=rtmp-pub:(.+)$")) this._subVoice = RegExp.$1;
								break;
							case 2:
								if(line.match("^a=rtmp-sub:(.+)$")) this._pubVideo = RegExp.$1;
								if(line.match("^a=rtmp-pub:(.+)$")) this._subVideo = RegExp.$1;
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

			if(this._pubVoice || this._pubVideo) this._swf.publish(   this._pubVoice, this._pubVideo );
			if(this._subVoice || this._subVideo) this._swf.subscribe( this._subVoice, this._subVideo );

			if(typeof offerSdp === "string") {
				this._listener.onMediaMessage(MediaEvent.SDP_ANSWER, {
					callId: callId,
					pointId: pointId,
					sdp: offerSdp
				});
			}
		}

	   	dispose(){
			this._swf.unpublish();
			this._swf.unsubscribe();
		}

		playDtmf(dtmf: string) {

		}
	}



	/**
	 * WebrtcMedia
	 *   webrtc media implementation
	 *
	 */
	export class WebrtcMedia implements IMedia {
		private _localStream: any = undefined;
		private _remoteStream: any = undefined;         /// TODO: this should be an array!

		private _remoteVideoStream: any = undefined;

		private __pc: any = null;
		private __pcVideo: any = null;
		private _localVideo: any = null;
		private _remoteVideo: any = null;

		private _localAudio: any = null;
		private _remoteAudio: any = null;

		private _soundVolume: number = 0.8;
		private _micVolume: number = 1;

		private _dtmfPlayer: DtmfPlayer = new DtmfPlayer();

		constructor(private _listener: IMediaListener, private _container) {
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

		start() {
			// notify ready immediately
			this._notifyReady();
		}

		_notifyReady() {
			if(this._listener && typeof(this._listener.onMediaReady) === "function")
				this._listener.onMediaReady(this);
		}

		_notify(type, data) {
			if(this._listener && typeof(this._listener.onMediaMessage) === "function")
				try{
					this._listener.onMediaMessage(type, data);
				}catch(err) {
					ERROR(err);
				}
		}

		getLocalAudioTrack() {
			return this._localStream && this._localStream.getAudioTracks &&
				this._localStream.getAudioTracks().length && this._localStream.getAudioTracks()[0];
		}

		setMicrophoneVolume(value:number) {
			//var audioTrack = this.getLocalAudioTrack();
			this._notify("HardwareEvent.MICROPHONE_VOLUME_CHANGED", { data: value });
		}

		setSoundVolume(value:number) {
			this._soundVolume = value;
			this._remoteAudio.volume = value;
			this._notify("HardwareEvent.SOUND_VOLUME_CHANGED", { data: value });
		}

		muteMicrophone(value:boolean) {
			this.getLocalAudioTrack().enabled = !value;//(value ? 0 : this._micVolume/100);
		}

		muteSound(value:boolean) {
			this._remoteAudio.volume = (value ? 0 : this._soundVolume);
		}

		getCc() {
			return {
				"userAgent" : "Chrome",
				"audio" : ["PCMA/8000"],                //["ISAC/16000", "opus/48000" , "PCMA/8000", "PCMU/8000"],
				"video" :  ["VP8/90000"],
				"profile" : "RTP/SAVPF",
				"ice": true,
				"rtcpMux" : true,
				"ssrcRequired" : true,
				"bundle": false
			};
		}

	 	dispose(){
			try{ this._localStream.getVideoTracks()[0].stop(); } catch(e) { LOG("Trouble on dispose video"); }
			try{ this._localStream.getAudioTracks()[0].stop(); } catch(e) { LOG("Trouble on dispose audio"); }
			this.__pcVideo = null;
			this.__pc = null;
		}

		checkHardware() {
			this._notify("HardwareEvent.HARDWARE_STATE", { data: {
				microphone: { state: HardwareState.DISABLED },
				camera: { state: HardwareState.DISABLED },
				userDefined : false
			}});
			getUserMedia(
				{
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
							{ height: VIDEO_HEIGHT},
							{ frameRate: VIDEO_FRAMERATE },
							{ facingMode: "user" }
						]
					}
				},
				(stream) => {
					var audioTracks = stream.getAudioTracks();
					var videoTracks = stream.getVideoTracks();
					this._localStream = stream;
					this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", { data: {
						microphone: {
							state: audioTracks.length ? HardwareState.ENABLED : HardwareState.ABSENT,
							wtfName: audioTracks.length ? audioTracks[0].label : ""
						},
						camera: {
							state: videoTracks.length ? HardwareState.ENABLED : HardwareState.ABSENT,
							wtfName: videoTracks.length ? videoTracks[0].label : ""
						},
						userDefined: true
					}});

				},
				(err) => {
					LOG("error getting mediastream", err);
					this._localStream = undefined;
					this._notify("HardwareEvent.HARDWARE_STATE", { data: {
						microphone: { state: HardwareState.DISABLED },
						camera: { state: HardwareState.DISABLED },
						userDefined: true
					}});
				});
		}

		__getAudioPeerConnection() {
			assert(this._localStream);
			if(this.__pc) return this.__pc;
			var pc_config = {"iceServers": []};
			var pc_constraints = {"optional": [{"DtlsSrtpKeyAgreement": false}]};
			this.__pc = new RTCPeerConnection(pc_config, pc_constraints);
			this.__pc.onaddstream = (event) => {
				this._remoteStream = event.stream;
				attachMediaStream(this._remoteAudio, event.stream);
			};
			this.__pc.onremovestream = function(event) {};
			this.__pc.addStream(this._localStream);
			return this.__pc;
		}

		__getVideoPeerConnection() {
			assert(this._localStream);
			if(this.__pcVideo) return this.__pcVideo;
			var pc_config = {"iceServers": []};
			var pc_constraints = {"optional": [{"DtlsSrtpKeyAgreement": false}]};
			this.__pcVideo = new RTCPeerConnection(pc_config, pc_constraints);
			this.__pcVideo.onaddstream = (event) => {
				this._remoteVideoStream = event.stream;
				attachMediaStream(this._remoteVideo, event.stream);
			};
			this.__pcVideo.onremovestream = function(event) {};
			this.__pcVideo.addStream(this._localStream);
			return this.__pcVideo;
		}

		setOfferSdp(callId: string, pointId: string, offerSdp: any) {
			LOG("Offer Sdp=\n" + offerSdp);
			var headLines: string[] = [], audioLines: string[] = [], videoLines: string[] = [];
			var lines: string[] = offerSdp.split("\r\n");
			var state = 0;
			for(var i=0; i<lines.length; i++) {
				var line: string = lines[i];
				if(line.match(/^m=audio/)) state = 1;
				if(line.match(/^m=video/)) state = 2;
				if(line) {
					switch(state) {
						case 0: headLines.push(line); break;
						case 1: audioLines.push(line); break;
						case 2: videoLines.push(line); break;
					}
				}
			}
			var head: string = headLines.join("\r\n") + "\r\n",
			    audio: string = audioLines.length ? audioLines.join("\r\n") + "\r\n" : "",
			    video: string = videoLines.length ? videoLines.join("\r\n") + "\r\n" : "";

			var audioAnswerSdp: string = "";
			var videoAnswerSdp: string = "";

			var answerReady = () => {
				if(audio && !audioAnswerSdp || video && !videoAnswerSdp) {
					return;
				}

				var answerLines: string[] = [];
				var headSdp = audioAnswerSdp || videoAnswerSdp;

				var lines = headSdp.split("\r\n");
				var state = 0;
				for(var i=0; i < lines.length; i++) {
					var line = lines[i];
					if(line.match(/^m=audio/)) state = 1;
					if(line.match(/^m=video/)) state = 2;
					if(line) {
						switch(state){
							case 0: if(!line.match(/^a=msid-semantic/)) answerLines.push(line); break;
							case 1: break;
							case 2: break;
						}
					}
				}

				if(audioAnswerSdp) {
					var lines = audioAnswerSdp.split("\r\n");
					var state = 0;
					for(var i=0; i < lines.length; i++) {
						var line = lines[i];
						if(line.match(/^m=audio/)) state = 1;
						if(line.match(/^m=video/)) state = 2;
						if(line) {
							switch(state){
								case 0: break;
								case 1: answerLines.push(line); break;
								case 2: break;
							}
						}
					}
				}

				if(videoAnswerSdp) {
					var lines = videoAnswerSdp.split("\r\n");
					var state = 0;
					for(var i=0; i < lines.length; i++) {
						var line = lines[i];
						if(line.match(/^m=audio/)) state = 1;
						if(line.match(/^m=video/)) state = 2;
						if(line) {
							switch(state){
								case 0: break;
								case 1: break;
								case 2: answerLines.push(line); break;
							}
						}
					}
				}

				var answerSdp = answerLines.join("\r\n") + "\r\n";
				LOG("We have FINISHED building answer SDP\n"+ answerSdp);
				this._listener.onMediaMessage(MediaEvent.SDP_ANSWER, {
					callId: callId,
					pointId: pointId,
					sdp: answerSdp
				});
			};

			var audioAnswerReady = (sdp) => {
				audioAnswerSdp = sdp;
				answerReady();
			};

			var videoAnswerReady = (sdp) => {
				videoAnswerSdp = sdp;
				answerReady();
			};


			if(audio) {
				var pcAudio = this.__getAudioPeerConnection();
				var mediaConstraints = {'mandatory': {'OfferToReceiveAudio': true, 'OfferToReceiveVideo': false}};
				var remoteSDP = new RTCSessionDescription({sdp:/*offerSdp*/ head+audio, type: 'offer'});
				pcAudio.setRemoteDescription(remoteSDP);
				LOG("pc.CreatingAnswer: AUDIO", pcAudio);
				pcAudio.createAnswer((localSDP) => {
					LOG("Answerer SDP (audio)=\n", localSDP.sdp);
					pcAudio.setLocalDescription(localSDP);
					audioAnswerReady(localSDP.sdp);
				}, null, mediaConstraints);
			}

			if(video) {
				var pcVideo = this.__getVideoPeerConnection();
				var mediaConstraints = {'mandatory': {'OfferToReceiveAudio': false, 'OfferToReceiveVideo': true}};
				var remoteSDP = new RTCSessionDescription({sdp:/*offerSdp*/ head+video, type: 'offer'});
				pcVideo.setRemoteDescription(remoteSDP);
				LOG("pc.CreatingAnswer: VIDEO", pcVideo);
				pcVideo.createAnswer((localSDP) => {
					LOG("Answerer SDP (video)=\n", localSDP.sdp);
					pcVideo.setLocalDescription(localSDP);
					videoAnswerReady(localSDP.sdp);
				}, null, mediaConstraints);
			}
		}
		playDtmf(dtmf: string) {
			this._dtmfPlayer.playDtmf(dtmf);
		}
	}


	/**
	 *  DtmfPlayer
	 *    play dtmf codes
	 *
	 *
	 */
	class ToneGenerator {
		private oscillator: any;
		private gainNode: any;

		constructor(private audioContext: any, private freq: number) {
			var volume = audioContext.createGainNode();
			volume.gain.value = 0.5;
			volume.connect(audioContext.destination);
			this.gainNode = volume;

			this.oscillator = this.audioContext.createOscillator();
			this.oscillator.type = 0;
			this.oscillator.frequency.value = freq;
			this.oscillator.noteOn(0);
		}
		play() {
			this.oscillator.connect(this.gainNode);
		}
		pause() {
			this.oscillator.disconnect();
		}
	}

	class DtmfPlayer {
		private _audioContext: any = null;
		private _oscillators: Object = {};
		private _queue: string[] = [];
		private _timerId: any = null;
		private static _tones = {
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

		constructor() {
			if(window["webkitAudioContext"]) {
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
				}
			}
			this._timerId = window.setInterval(() => {
				if(this._queue.length) {
					this.__playDtmfCharacter(this._queue.splice(0,1)[0])
				}
			}, DTMF_INTERVAL);
		}

		playDtmf(dtmf: string) {
			for(var i: number = 0; i < dtmf.length; i++) {
				var c:string = dtmf.toString().charAt(i).toUpperCase();
				if(DtmfPlayer._tones.hasOwnProperty(c)){
					this._queue.push(c);
				} else { /* invalid DTMF character */ }
			}
		}

		__playDtmfCharacter(c: string) {
			if(DtmfPlayer._tones.hasOwnProperty(c) && this._audioContext) {
				var freq1: number = <any>DtmfPlayer._tones[c][1];
				var freq2: number = <any>DtmfPlayer._tones[c][0];

				var oscillator1 = this._oscillators[freq1];
				var oscillator2 = this._oscillators[freq2];

				oscillator1.play();
				oscillator2.play();
				window.setTimeout(() => {
					oscillator1.pause();
					oscillator2.pause();
				}, DTMF_DURATION);
			}
		}
	}
}