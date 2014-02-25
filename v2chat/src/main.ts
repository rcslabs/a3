/// <reference path="communicator.ts" />
/// <reference path="jquery.d.ts" />

var STUN_SERVER = "stun:stun.l.google.com:19302";
var STUN_TIMEOUT = 3000;
var FP_MIN_VERSION = "10.3";

declare var LOG:any;
declare var WARN:any;
declare var ERROR:any;
declare var debug:any;
declare var DEBUG_ENABLED:boolean;

class Click2CallCommunicator extends a3.Communicator {

    private _destination:string = null;
    private _mediator:Mediator = null;
    private _username:string = null;
    private _password:string = null;
    private _deferredCall:any = null;
	public query:any = {};
	public locale:any = null;

    constructor() {
        super();
		// parse query string if any
		if("" !== document.location.search){
			document.location.search.replace(/^[?]/, "").split("&").forEach((e) => { var a = e.split('='); this.query[a[0]] = a[1]; });
		}
		if(typeof this.query['id'] === 'undefined'){   this.query.id = 'default'; }
		if(typeof this.query['lang'] === 'undefined'){ this.query.lang = 'en'; }
    }

	setLocale(data:any){
		this.locale = data[this.query.lang];
	}

    setFactory(factory:a3.ICommunicatorFactory){
        super.setFactory(factory);
		(<CompatibleFactory>(this.factory)).setListener(this);
		(<CompatibleFactory>(this.factory)).setProjectId(this.query.id);
    }

    // overrides super start
    start(){
		this._mediator = new Mediator(this, document.getElementById('a3'));
		(<CompatibleFactory>(this.factory)).checkCompatibility();
    }

    onCommunicatorFactoryReady() {
        super.start()
    }

    setDestination(value:string){
        this._destination = value;
    }

    setCredentials(username:string, password:string) {
        this._username = username;
        this._password = password;
    }

    onCommunicatorStarting(){
        this._mediator.onCommunicatorStarting();
    }

    // autoconnect on application started
    onCommunicatorStarted(){
        this._mediator.onCommunicatorStarted();
        this.connect();
    }

    onCommunicatorFailed() {
        this._mediator.onCommunicatorFailed();
    }

    onConnecting() {
        this._mediator.onConnecting();
    }

    onConnected() {
		this._mediator.onConnected();
    }

    onConnectionFailed(){
        // uncomment below for connection auto failover
        // this.connect();
    }

    onSessionStarted() {
        if(this._deferredCall != null){
            this.media.checkHardware(this._deferredCall.vv[1]);
        }
    }

    onCheckHardwareSettings() {
        this._mediator.onCheckHardwareSettings();
    }

    onCheckHardwareReady() {
        this.setSoundVolume(0.8); // TODO: more gentle setup
        this._mediator.onCheckHardwareReady();
        if(this._deferredCall != null){
            var vv = this._deferredCall.vv;
            var destination = this._deferredCall.destination;
            this._deferredCall = null;
            super.startCall(destination, vv);
        }
    }

    onCheckHardwareFailed() {
        this._mediator.onCheckHardwareFailed();
    }

    onSoundVolumeChanged(value:number) {
        this._mediator.onSoundVolumeChanged(value);
    }

    onCallStarting(call: a3.Call) {
        this._mediator.onCallStarting(call);
    }

    onCallStarted(call: a3.Call) {
        this._mediator.onCallStarted(call);
    }

    onCallFinished(call: a3.Call) {
        this.media.dispose();
        this._mediator.onCallFinished(call);
    }

    onCallFailed(call: a3.Call) {
        this.media.dispose();
        this._mediator.onCallFailed(call);
    }

    // click call -> start session (if not ready) -> check hardware (if not ready) -> start call
    onClickStartCall(type){
        var vv = [false, false];
        if(type == a3.CallType.AUDIO || type == a3.CallType.BOTH){ vv[0] = true; }
        if(type == a3.CallType.VIDEO || type == a3.CallType.BOTH){ vv[1] = true; }

        this._deferredCall = {destination: this._destination, vv: vv};
        // start session if not ready, then check hardware async and so on...
        if(this.getState() !== a3.State.SESSION_STARTED){
            this.open(this._username, this._password, '', '');
        }else{
            this.media.checkHardware(vv[1]);
        }
    }

    onClickHangupCall(){
        if(this.calls.length < 1){ return; }
        var call:a3.Call = this.calls[0]; // only single call allowed
        call.hangup();
    }

    onClickKeypadChar(value:string){
        if(this.calls.length < 1){ return; }
        var call:a3.Call = this.calls[0]; // only single call allowed
        this.media.playDtmf(value);
        call.sendDTMF(value);
    }

	onClickMicMute(value:boolean){
		this.media.muteMicrophone(value);
	}

	onClickSoundMute(value:boolean){
		this.media.muteSound(value);
	}
}

class MediaContainer {
    private _x:number = 0;

    constructor(private _elem:HTMLElement, private _root:HTMLElement){
		this.hide();
		//$(this._elem).css('visibility', 'visible');
	}

    isFlash():boolean{
        return ((<HTMLElement>(this._elem.firstChild)).id == "a3-swf-media");
    }

	showSettingsPanel(){
		if(!this.isFlash()) return; // no matter if media is not flash
		var $el = $(this._elem);
		$el.removeClass('a3-remote-video');
		$el.removeClass('a3-remote-video-hidden');
		$el.addClass('a3-settings-panel');
	}

    show(){
		var $el = $(this._elem);
		$el.removeClass('a3-remote-video-hidden');
		$el.removeClass('a3-settings-panel');
		$el.addClass('a3-remote-video');
    }

    hide(){
		var $el = $(this._elem);
		$el.removeClass('a3-settings-panel');
		$el.removeClass('a3-remote-video');
		$el.addClass('a3-remote-video-hidden');
    }
}

class Mediator implements a3.ICommunicatorListener {

	private _width:number;
	private _height:number;
    private _media:MediaContainer;
    private _communicator:Click2CallCommunicator;
    private _root:any = null; // should be DIV
    private _views:{ [id: string]: any; } = {}; // should be DIVs
    private _elems:{ [id: string]: any; } = {}; // any DOM nodes

	private _stopwatchValue: number = 0;
	private _stopwatchIntv: number = 0;

    constructor(communicator:Click2CallCommunicator, rootElement:HTMLElement){
        this._communicator = communicator;
        this._root = rootElement;
		this._width = $(rootElement).width();
		this._height = $(rootElement).height();
        this._initTemplate();
    }

    onCommunicatorStarting() {
        this._setStatus('loading');
    }

    onConnecting() {
        this._setStatus('connecting');
    }

    onConnected() {
		if(this._views['intro'] != null){
        	this._toggleView('intro');
		} else { // if intro disabled - start the video call immediately
			this._communicator.onClickStartCall(a3.CallType.BOTH);
		}
        this._setStatus('connected');
    }

    onConnectionFailed() {
        this._setStatus('connection failed');
    }

    onCheckHardwareSettings() {
        this._setStatus('check hardware');
        this._toggleView('hw-test');
		this._media.showSettingsPanel();
    }

    onCheckHardwareReady() {
        this._setStatus('hardware ready');
		this._media.hide();
    }

    onCheckHardwareFailed() {
        this._setStatus('hardware test was failed');
        this._toggleView('hw-failed');
    }

    onSoundVolumeChanged(value:number) {
		if(!$('.a3-hardware-controls').length) return; // no hardware controls
        (<any>$('.sound-volume-slider')).simpleSlider('setValue', value);
    }

    onCallStarting(call:a3.Call) {
        this._setStatus('call connecting');
        this._toggleView('calling');
    }

    onCallStarted(call:a3.Call) {
        this._setStatus('talking');
        this._toggleView('talking');
        if(call.isVideo()){
            this._media.show();
        }
    }

    onCallFinished(call:a3.Call) {
        this._media.hide();
        this._setStatus('ready');
        this._toggleView('call-finished');
    }

    onCallFailed(call:a3.Call) {
        this._media.hide();
        this._setStatus('ready');
        this._toggleView('call-failed');
    }

    onCommunicatorStarted() {
        this._media = new MediaContainer((<CompatibleFactory>(this._communicator.factory)).mediaContainer, this._root);
    }

    onCommunicatorFailed() {
        this._setStatus('app failed');
        this._toggleView('loading-failed');
    }

    onSessionStarting() {}
    onSessionStarted() {}
    onSessionFailed() {}
    onIncomingCall(call:a3.Call) {}

    private _e(id:string){
        return document.getElementById(id);
    }

    _initTemplate() {
        this._views['loading']        = this._e('a3-loading-view');
        this._views['loading-failed'] = this._e('a3-loading-failed-view');
        this._views['intro']          = this._e('a3-intro-view');
        this._views['hw-test']        = this._e('a3-hw-test-view');
        this._views['hw-failed']      = this._e('a3-hw-failed-view');
        this._views['start-call']     = this._e('a3-start-call-view');
        this._views['calling']        = this._e('a3-calling-view');
        this._views['talking']        = this._e('a3-talking-view');
        this._views['call-failed']    = this._e('a3-call-failed-view');
        this._views['call-finished']  = this._e('a3-call-finished-view');
		this._views['callback']       = this._e('a3-callback-view');
		this._views['callback-result']= this._e('a3-callback-result-view');
        this._views['outro']          = this._e('a3-outro-view');

        for(var p in this._views){
            if(this._views[p] == null){ continue; }
			this._views[p].style.visibility = 'hidden';
			$(this._views[p]).addClass('a3-view');
        }

		if($('.a3-hardware-controls').length){
			this._initHardwareControls();
		}

		this._initFooter();

		if('he' == this._communicator.query.lang){ this._root.style.direction = 'rtl'; }
        this._root.addEventListener('click', (e) => {this._onClick(<MouseEvent>e)});
        this._root.style.display = 'none';
    }

    _setStatus(message:string){
        // TODO: localize messages and all that jazz...
        // this._elems['statusbar'].innerHTML = message;
    }

    _rect(elem:any):any{
        var a = [0,0,0,0]; // x,y,w,h
        a[0] = parseInt(elem.style.left);
        a[1] = parseInt(elem.style.top);
        a[2] = parseInt(elem.style.width);
        a[3] = parseInt(elem.style.height);
        return a;
    }

	_initFooter(){
		var e = document.createElement('style');
		var h = '#a3-footer{position:absolute;width:100%;left:0;bottom:0px;background-color: #d4e4f1; font-size:12px;font-weight:normal;}\n';
		    h+= '.a3-btn-back{position:absolute;bottom:32px;left:10px}\n';
		    h+= '.a3-btn-help{position:absolute;bottom:32px;right:10px}\n';
		    h+= '\n';
		e.innerHTML = h;
		document.head.appendChild(e);

		var l = this._communicator.locale;
		var copy = (typeof l['COPYRIGHT']  !== 'undefined' ? l['COPYRIGHT']  : '&copy;');
		var back = (typeof l['BACK_LABEL'] !== 'undefined' ? l['BACK_LABEL'] : 'BACK');
		var help = (typeof l['HELP_LABEL'] !== 'undefined' ? l['HELP_LABEL'] : 'HELP');
		var h = '<div id="a3-footer"><button class="a3-btn-back a3-footer-button">'+back+'</button><button class="a3-btn-help a3-footer-button">'+help+'</button><div style="padding:4px;text-align:right;">'+copy+'</div></div>';
		$(this._root).append(h);
	}

	_initHardwareControls(){
		// TODO: hardcoded default slider value is not good!
		var h = '<table align="center" cellspacing="0"><tr>' +
			'<td style="background-image: url(\'bg-alpha.png\')"><button style="margin:4px 6px auto 6px;width:25px;height:30px;background: url(\'sprite.png\') -25px 0;border: none;" class="a3-btn-mic-mute"></button></td>' +
			'<td>&nbsp;</td>' +
			'<td style="background-image: url(\'bg-alpha.png\')"><button class="a3-btn-sound-mute" style="margin-top:4px;width:30px; height:30px; background: url(\'sprite.png\') -80px 0; border: none;"></button></td>' +
			'<td style="background-image: url(\'bg-alpha.png\');padding:0 8px;"><input class="sound-volume-slider" data-slider="true" value="0.8" data-slider-highlight="true" data-slider-theme="volume" data-slider-range="0,1" data-slider-step="0.1" type="text"></td>' +
			'</tr></table>';

		$('.a3-hardware-controls').css('width', '99%').html(h);

		// $(document.head).append('<link href="slider/simple-slider.css" rel="stylesheet" type="text/css" />');
		// workaround below for pretty rendering
		$.get("slider/simple-slider.css", function(_) {
			$(document.head).append($('<style></style>').html(_));
			$(document.body).append('<script src="slider/simple-slider.js"></script>');
		});

		$('#a3').on('slider:changed', (e, data) => { this._communicator.setSoundVolume(data.value); });

		$('.a3-btn-mic-mute, .a3-btn-sound-mute').attr('data-toggle', 'true');
		$('#a3').on('click', '.a3-btn-mic-mute', function(e){
			var t = !('true' == $(this).attr('data-toggle'));
			$(this).attr({'data-toggle': t});
			$(this).css('background-position', (t ? '-25px 0px' : '0px 0px'));
		});

		$('#a3').on('click', '.a3-btn-sound-mute', function(e){
			var t = !('true' == $(this).attr('data-toggle'));
			$(this).attr({'data-toggle': t});
			$(this).css('background-position', (t ? '-80px 0px' : '-50px 0px'));
		});
	}

    _onClick(event:MouseEvent){
        //LOG(event);
        var cls = event.target['className'];
        if(cls === undefined){ return; }
        var m = cls.match(/a3-([^ ]+)/);
        if(m == null || m.length<2){ return; }
        var cls = m[1];
        switch(cls){
			case 'btn-back':
				this._communicator.onClickHangupCall();
				this._toggleView('intro');
				break;
			case 'btn-help':
				this._toggleHelp();
				break;
			case 'btn-callback':
				this._toggleView('callback');
				break;
            case 'btn-voice-call':
                this._toggleView('start-call');
                this._communicator.onClickStartCall(a3.CallType.AUDIO);
                break;
            case 'btn-video-call':
                this._toggleView('start-call');
                this._communicator.onClickStartCall(a3.CallType.BOTH);
                break;
            case 'btn-hangup':
            case 'btn-cancel':
                this._communicator.onClickHangupCall();
                break;
			case 'btn-mic-mute':
				this._communicator.onClickMicMute(!('true' == $(event.target).attr('data-toggle')));
				break;
			case 'btn-sound-mute':
				this._communicator.onClickSoundMute(!('true' == $(event.target).attr('data-toggle')));
				break;
			default:
				WARN("Unhandled click on elem ." + cls);
        }
    }

    _toggleView(id:string):void{
        for(var p in this._views){
			if(this._views[p] == null){ continue; }
            this._views[p].style.visibility = (p == id ? 'visible' : 'hidden');
        }
		$('body').trigger('view-changed', id);
        this._root.style.display = 'block';

		if(-1 != ['hw-failed', 'call-failed', 'call-finished', 'callback', 'callback-result', 'outro'].indexOf(id)){
			$('.a3-btn-back').removeAttr('disabled');
		}else{
			$('.a3-btn-back').attr('disabled','disabled');
		}

		/* start/stop stopwatch */
		var $stopwatch = $('.a3-stopwatch');
		if($stopwatch.length){
			if('talking' == id){
				this._stopwatchValue = 0;
				this._stopwatchIntv = setInterval(() => {
					$stopwatch.html(this._toMMSS(this._stopwatchValue++));
				}, 1000);
			}else{
				clearInterval(this._stopwatchIntv);
			}
		}
    }

	_toMMSS(value:number) {
		var hours:any   = Math.floor(value / 3600);
		var minutes:any = Math.floor((value - (hours * 3600)) / 60);
		var seconds:any = value - (hours * 3600) - (minutes * 60);
		if (minutes < 10) {minutes = "0"+minutes;}
		if (seconds < 10) {seconds = "0"+seconds;}
		var time = minutes+':'+seconds;
		return time;
	}

	_toggleHelp(){
		console.log('HELP');
	}
}


class ConstructorCompatibleSioSignaling extends a3.SioSignaling
{
	constructor(listener: a3.ISignalingListener, private _projectId:string) {
		super(listener);
	}

	// override
	open(username: string, password: string, challenge: string, code: string) {
		var data = {username: username, password: password};
		if(this._projectId != null){ data['projectId'] = this._projectId; }
		this.request("START_SESSION", data);
	}

	// override
	startCall(bUri: string, cc: Object, vv: boolean[]) {
		bUri = bUri || (vv[1] ? '5678' : '1234');
		var data:any = {sessionId:this.sessionId, aUri:"", bUri:bUri, cc:cc, vv:vv};
		if(this._projectId != null){ data['projectId'] = this._projectId; }
		this.request("START_CALL", data);
	}
}


class ConstructorCompatibleFlashSignaling extends a3.FlashSignaling {

	// override
	open(username: string, password: string, challenge: string, code: string) {
		super.open('USER', 'PASS', challenge, code);
	}
}


class CompatibleFactory implements a3.ICommunicatorFactory
{
	private _endpoints:string[] = [];
	private _serviceName:string = null;
	private _listener:any = null;
	private _projectId:string = null;
	private _logLevel:string = 'NONE';
	public hasWRTC:boolean = false;
	public hasFlash:boolean = false;
	public mediaContainer:HTMLElement;

	constructor(){
		var e = document.createElement('style');
		var h = '.a3-settings-panel{width:220px;height:140px;left:50%;top:50%;margin:-70px auto auto -110px}\n';
		h+= '.a3-remote-video-hidden{left:-999px}';
		e.innerHTML = h;
		document.head.appendChild(e);
		this.mediaContainer = document.createElement('div');
		this.mediaContainer.id = 'a3-media-container';
		this.mediaContainer.style.position = 'absolute';
		this.mediaContainer.style.zIndex = '50';
		this.mediaContainer.className = 'a3-remote-video-hidden';
		//this.mediaContainer.style.visibility = 'hidden';
		document.body.appendChild(this.mediaContainer);
		if(DEBUG_ENABLED) this._logLevel = 'ALL';
	}

	addEndpoint(value:string){
		this._endpoints.push(value);
	}

	setProjectId(value:string){
		this._projectId = value;
	}

	setServiceName(value:string){
		this._serviceName = value;
	}

	setListener(listener: any){
		this._listener = listener;
	}

	checkCompatibility(){
		this.hasFlash = window['swfobject']['hasFlashPlayerVersion'](FP_MIN_VERSION);
		var endpoints = this._endpoints.filter(function(e){return 0 == e.indexOf('http')});
		if(endpoints.length){ // check WRTC compatibility only for http signaling
			this._checkWebrtcAvailability();
		}else{
			this._onWRTCCapabilityFailed();
		}
	}

	createMedia(listener: a3.IMediaListener):a3.IMedia {
		if(this.hasWRTC){
			return <a3.IMedia>(new a3.WebrtcMedia(listener, this.mediaContainer));
		}else if(this.hasFlash){
			return <a3.IMedia>(new a3.FlashMedia(listener, this.mediaContainer, {logLevel:this._logLevel}));
		}else{
			return null;
		}
	}

	createSignaling(listener: a3.ISignalingListener):a3.ISignaling {
		var signaling:a3.ISignaling = null;
		var endpoints:string[] = [];
		if(this.hasWRTC){ // for WRTC we always use http signaling
			// find very first http in endpoints
			endpoints = this._endpoints.filter(function(e){return 0 == e.indexOf('http')});
			if(!endpoints.length) throw new Error("No any http endpoints");
			signaling = <a3.ISignaling>(new ConstructorCompatibleSioSignaling(listener, this._projectId));
			signaling.addEndpoint(endpoints[0]);
			signaling.setService(this._serviceName);
		}else{
			// find all rtmp endpoints
			endpoints = this._endpoints.filter(function(e){return 0 == e.indexOf('rtmp')});
			if(!endpoints.length) return null;
			var flashContainer = document.createElement('div');
			flashContainer.id = "a3-signaling-flash";
			document.body.appendChild(flashContainer);
			signaling = <a3.ISignaling>(new ConstructorCompatibleFlashSignaling(listener, flashContainer.id, {logLevel:this._logLevel}));
			// set all rtmp endpoints into signaling
			while(endpoints.length){ signaling.addEndpoint(endpoints.shift()); }
		}
		return signaling;
	}

	private _onWRTCCapabilitySuccess(){
		this.hasWRTC = true;
		this._listener.onCommunicatorFactoryReady();
	}

	private _onWRTCCapabilityFailed(){
		this.hasWRTC = false;
		this._listener.onCommunicatorFactoryReady();
	}

	private _checkWebrtcAvailability() {
		if(!navigator['webkitGetUserMedia'] || !window['webkitRTCPeerConnection']) {
			this._onWRTCCapabilityFailed();
			return;
		}

		var flashTimeout = window.setTimeout(()=> {
			flashTimeout = null;
			this._onWRTCCapabilityFailed();
			peerConnection.close();
		}, STUN_TIMEOUT);

		var peerConnection = new window['webkitRTCPeerConnection'](
			{ "iceServers": [{ "url": STUN_SERVER }] }
		);

		peerConnection.onicecandidate = (event) => {
			if(flashTimeout) {
				if(event.candidate) {
					var c = event.candidate.candidate;
					LOG(event.candidate);
					if(c.indexOf(" udp ") !== -1 && c.indexOf(" typ srflx ") !== -1) {
						// This is address the STUN server returned to
						// srflx :  Server reflexive address is the NATed IP address
						clearTimeout(flashTimeout);
						flashTimeout = null;
						this._onWRTCCapabilitySuccess();
						peerConnection.close();
					}
				} else {
					// all candidates are now
					LOG("Stun finished: NO srflx: starting flash");
					clearTimeout(flashTimeout);
					flashTimeout = null;
					this._onWRTCCapabilityFailed();
					peerConnection.close();
				}
			}
		};

		peerConnection.createOffer(function (sessionDescription) {
			peerConnection.setLocalDescription(sessionDescription);
		}, null, { 'mandatory': { 'OfferToReceiveAudio': true, 'OfferToReceiveVideo': false } });
	}
}


class Resources {
	private _tmplUrl:string = null;
	private _localeUrl:string = null;
	public tmpl:any = null;
	public locale:any = null;

	constructor(private _baseUrl: string){
		this._tmplUrl = this._baseUrl+"/template.html";
		this._localeUrl = this._baseUrl+"/locale.ini";
	}

	loadTemplate(cb:any) {
		$.ajax({ url: this._tmplUrl })
			.done((data) => {
				this.tmpl = data;
				cb.call(null, this.tmpl);
			}).fail(() => { cb.call(null, false); });
	}

	saveTemplate(tmpl:string) {
		$.ajax({
			type: "PUT",
			url: this._tmplUrl,
			contentType: "text/html",
			data: tmpl
		}).fail((e) => { alert("Error "+ e.status); });
	}

	loadLocale(cb:any, parse:boolean=false) {
		$.ajax({url: this._localeUrl})
			.done((data) => {
				this.locale = (parse ? parseIni(data) : data);
				cb.call(null, this.locale);
			}).fail(() => { cb.call(null, false); });
	}

	saveLocale(locale:string) {
		$.ajax({
			type: "PUT",
			url: this._localeUrl,
			contentType: "text/plain",
			data: locale
		}).fail((e) => { alert("Error "+ e.status); });
	}

	// load all resources and prepare template with locale if any
	load(cb:any, lang:string = null) {
		var _this = this;
		var baseUrlRE = new RegExp('[$][{]BASE_URL[}]', 'mg');
		var _cb:any = (data) => {
			if(data === false){ // fail
				cb.call(null, false);
			}else{
				if(lang !== null){ // if lang defined
					if(_this.tmpl != null && _this.locale != null){ // if loaded prepare content
						for(var p in _this.locale[lang]){
							_this.tmpl = _this.tmpl.replace(new RegExp('[$][{]'+p+'[}]', 'mg'), _this.locale[lang][p]);
						}
						_this.tmpl = _this.tmpl.replace(baseUrlRE, _this._baseUrl);
						cb.call(null, _this);
					}
				}else{
					if(_this.tmpl != null){
						_this.tmpl = _this.tmpl.replace(baseUrlRE, _this._baseUrl);
						cb.call(null, _this);
					}
				}
			}
		};
		this.loadTemplate(_cb);
		if(lang !== null){ this.loadLocale(_cb, true); }
	}
}

function parseIni(data){
	var regex = {
		section: /^\s*\[\s*([^\]]*)\s*\]\s*$/,
		param: /^\s*([\w\.\-\_]+)\s*=\s*(.*?)\s*$/,
		comment: /^\s*;.*$/
	};
	var value = {};
	var lines = data.split(/\r\n|\r|\n/);
	var section:string = null;
	lines.forEach(function(line){
		if(regex.comment.test(line)){
			return;
		}else if(regex.param.test(line)){
			var match = line.match(regex.param);
			if(section){
				value[section][match[1]] = match[2];
			}else{
				value[match[1]] = match[2];
			}
		}else if(regex.section.test(line)){
			var match = line.match(regex.section);
			value[match[1]] = {};
			section = match[1];
		}else if(line.length == 0 && section){
			section = null;
		};
	});
	return value;
}

debug = function(value:boolean){ document.cookie = 'debug=' + value +';path=/';}
if(-1 != document.cookie.indexOf('debug=true')){ LOG = WARN = ERROR = console.log.bind(console); }
DEBUG_ENABLED = (-1 != document.cookie.indexOf('debug=true'));