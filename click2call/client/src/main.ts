/// <reference path="communicator.ts" />
/// <reference path="jquery.d.ts" />

var STUN_SERVER = "stun:stun.l.google.com:19302";
var STUN_TIMEOUT = 4000;
var FP_MIN_VERSION = "10.3";
var STAT_SERVICE = "https://webrtc.v2chat.com/stat/push/";
var CALLBACK_SERVICE = 'https://webrtc.v2chat.com/service/callback';

declare var LOG:any;
declare var WARN:any;
declare var ERROR:any;
declare var debug:any;
declare var DEBUG_ENABLED:boolean;
declare var worldTime:any;

class Click2CallCommunicator extends a3.Communicator {

    private _destination:string = null;
    private _mediator:Mediator = null;
    private _username:string = null;
    private _password:string = null;
    private _deferredCall:any = null;
    private _statCookie:string = null;
	// business hours feature
	private _timezone:string = null;
	private _rules = {};
	private _weekdays = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];

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
        var m = document.cookie.match(/A3Stat=(\d+)/);
        if(null != m){ this._statCookie = m[1]; }
        if(DEBUG_ENABLED){ STAT_SERVICE = CALLBACK_SERVICE = null; }
        this.sendStat('INIT');
    }

	setLocale(data:any){
		this.locale = data[this.query.lang];
	}

    setFactory(factory:a3.ICommunicatorFactory){
        super.setFactory(factory);
		(<CompatibleFactory>(this.factory)).setListener(this);
		(<CompatibleFactory>(this.factory)).setProjectId(this.query.id);
        if(this.query['operatorid'] != undefined){
            (<CompatibleFactory>(this.factory)).setOperatorId(this.query.operatorid);    
        }
    }

    // overrides super start
    start(){
		this._mediator = new Mediator(this, document.getElementById('a3'));
		this.onCommunicatorStarting();
		// if hours out of business we have no reason to start communicator
		if(!this.checkBusinessHours()){ // closed
			this._mediator.showCallbackForm(this.locale['OFFLINE_HOURS']);
		}else{
			(<CompatibleFactory>(this.factory)).checkCompatibility();
		}
    }

    onCommunicatorFactoryReady() {
		var err = (<CompatibleFactory>(this.factory)).compatibilityError;
		if(err != null){
			this.onCommunicatorFailed(err);
		}else{
			super.start();
		}
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
        this.sendStat('STARTED');
        this.connect();
    }

    onCommunicatorFailed(reason:string = null) {
        this._mediator.onCommunicatorFailed(reason);
        this.sendStat('FAILED', 'application');
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
        this.sendStat('FAILED', 'connection');
    }

	onSessionFailed() {
		if(this._deferredCall != null){
			this._mediator.onCallFailed(this._deferredCall);
			this.sendStat('FAILED', 'session');
			this._deferredCall = null;
		}
	}

    onSessionStarted() {
        if(this._deferredCall != null){
            this.media.checkHardware(this._deferredCall.vv);
        }
    }

    onCheckHardwareSettings() {
        this.sendStat('CHECK_HW');
        this._mediator.onCheckHardwareSettings();
    }

    onCheckHardwareReady() {
        this.setSoundVolume(0.8); // TODO: more gentle setup
        this._mediator.onCheckHardwareReady();
        if(this._deferredCall != null){
            var vv = this._deferredCall.vv;
            var destination = this._deferredCall.destination;
            this._deferredCall = null;
            this.sendStat('START_CALL');
            super.startCall(destination, vv);
        }
    }

    onCheckHardwareFailed() {
        this._mediator.onCheckHardwareFailed();
        this.sendStat('FAILED', 'hardware');
    }

    onSoundVolumeChanged(value:number) {
        this._mediator.onSoundVolumeChanged(value);
    }

    onCallStarting(call: a3.Call) {
        this._mediator.onCallStarting(call);
    }

    onCallStarted(call: a3.Call) {
        this.sendStat('CALL_STARTED');
        this._mediator.onCallStarted(call);
    }

    onCallFinished(call: a3.Call) {
        this.sendStat('CALL_FINISHED');
        this.media.dispose();
        this._mediator.onCallFinished(call);
    }

    onCallFailed(call: a3.Call) {
        this.sendStat('CALL_FAILED');
        this.media.dispose();
        this._mediator.onCallFailed(call);
    }

    // click call -> start session (if not ready) -> check hardware (if not ready) -> start call
    onClickStartCall(type){
        this.sendStat('CLICK_TO_CALL');
        var vv = [false, false];
        if(type == a3.CallType.AUDIO || type == a3.CallType.BOTH){ vv[0] = true; }
        if(type == a3.CallType.VIDEO || type == a3.CallType.BOTH){ vv[1] = true; }

        this._deferredCall = {destination: this._destination, vv: vv};
        // start session if not ready, then check hardware async and so on...
        if(this.getState() !== a3.State.SESSION_STARTED){
            this.open(this._username, this._password, '', '');
        }else{
            this.media.checkHardware(vv);
        }
    }

    onClickHangupCall(){
        if(this.calls.length < 1){ return; }
        var call:a3.Call = this.calls[0]; // only single call allowed
        call.hangup();
    }

    onClickDialpadChar(value:string){
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

    sendStat(ev:string, details:string=null){
        var data = {'ref': encodeURI(document.referrer)};
        data['e'] = ev;
        data['sc'] = this._statCookie;
        data['b'] = this.query.id;
        data['c'] = (this.calls[0] ? this.calls[0].getId() : null);
        data['ts'] = (new Date()).getTime();
        data['rnd'] = Math.random();
        data['details'] = details;
        if(STAT_SERVICE == null){ 
            LOG(data);
        } else {
            $.get(STAT_SERVICE, data);
        }
    }

	// business hours feature

	addRuleForOpened(dayOfWeek:string, timeToOpen:string, timeToClose:string)
	{
		if('*' == dayOfWeek){ // add rules for all weekdays
			this._weekdays.forEach((wd) => {
				// an existing rule for this day has high priority
				if(this._rules[wd] === undefined){
					this._rules[wd] = [timeToOpen, timeToClose];
				}
			});
		}else{
			this._rules[dayOfWeek] = [timeToOpen, timeToClose];
		}
	}

	setTimezone(timezone:string) {
		this._timezone = timezone;
	}

	checkBusinessHours()
	{
		// no problem, man. continue ok
		if(this._timezone == null){ return true; }
		var tz = this._timezone;
		// worldTime must define in time.js injected above this script
		var nowWday = worldTime[tz].substring(0, 3);
		var nowTime = worldTime[tz].substring(16, 21);
		var hhmmToAbsMinutes = function(hhmm){
			hhmm = hhmm.split(':');
			return 60*parseInt(hhmm[0], 10)+parseInt(hhmm[1], 10);
		};
		var ruleForToday = this._rules[nowWday];
		if(ruleForToday === undefined){
			return false;
		}
		var openAt =  hhmmToAbsMinutes(ruleForToday[0]);
		var closeAt = hhmmToAbsMinutes(ruleForToday[1]);
		var now =     hhmmToAbsMinutes(nowTime);
		var result =  (now >= openAt) && (now <= closeAt);
		//console.log(worldTime[this.tz], ruleForToday, nowTime, openAt, now, closeAt, result);
		//listener.call(null, result);
		return result;
	}
}

class MediaContainer {
    private _x:number = 0;

    constructor(private _elem:HTMLElement, private _root:HTMLElement){
		this.hide();
		//$(this._elem).css('visibility', 'visible');
	}

    isFlash():boolean {
        return ((<HTMLElement>(this._elem.firstChild)).id == "a3-swf-media");
    }

	showSettingsPanel(){
		if(!this.isFlash()) return; // no matter if media is not flash
		var $el:any = $(this._elem);
		$el.removeClass('a3-remote-video');
		$el.addClass('a3-settings-panel');
		var off = $('.a3-settings-panel-proxy').offset();
		$el.offset({'top':off.top});
	}

    show(){
		var $el:any = $(this._elem);
		$el.removeClass('a3-settings-panel');
		$el.addClass('a3-remote-video');
		var off = $('.a3-remote-video-proxy').offset();
		$el.offset({'top':off.top});
    }

    hide(){
		var $el:any = $(this._elem);
		$el.removeClass('a3-settings-panel');
		$el.removeClass('a3-remote-video');
		$el.offset({'top':-9999});
    }
}

class Mediator implements a3.ICommunicatorListener {

	private _width:number;
	private _height:number;
    private _media:MediaContainer;
    private _communicator:Click2CallCommunicator;
    private _root:any = null; // should be DIV
    private _views:{ [id: string]: any; } = {}; // should be DIVs
	private _$helpContainer:any = null;
	private _stopwatchValue: number = 0;
	private _stopwatchIntv: number = 0;
	private _currentView: string = null;

    constructor(communicator:Click2CallCommunicator, rootElement:HTMLElement){
        this._communicator = communicator;
        this._root = rootElement;
		this._width = $(rootElement).width();
		this._height = $(rootElement).height();
        this._initTemplate();
    }

    onCommunicatorStarting() {
		this._toggleView('loading');
	}

    onConnecting() {}

	onConnectionFailed() {}

    onConnected() {
		if(this._views['intro'] != null){
        	this._toggleView('intro');
		} else { // if intro disabled - start the video call immediately
			this._communicator.onClickStartCall(a3.CallType.BOTH);
		}
    }

    onCheckHardwareSettings() {
        this._toggleView('hw-test');
		this._media.showSettingsPanel();
    }

    onCheckHardwareReady() {
		this._media.hide();
    }

    onCheckHardwareFailed() {
        this._toggleView('hw-failed');
    }

    onSoundVolumeChanged(value:number) {
		if(!$('.a3-hardware-controls').length) return; // no hardware controls
        (<any>$('.sound-volume-slider')).simpleSlider('setValue', value);
    }

    onCallStarting(call:a3.Call) {
        this._toggleView('calling');
    }

    onCallStarted(call:a3.Call) {
        if(call.isVideo()){
			this._toggleView('talking-video');
            this._media.show();
        }else{
			this._toggleView('talking-voice');
		}
    }

    onCallFinished(call:a3.Call) {
        this._media.hide();
        this._toggleView('call-finished');
    }

    onCallFailed(call:a3.Call) {
        this._media.hide();
		var l = this._communicator.locale;
		if(this._views['call-failed'] != null){
            $('h2', this._views['call-failed']).remove();
			$('h1', this._views['call-failed']).after("<h2>"+l['CALL_FAILED']+"</h2>");
        	this._toggleView('call-failed');
		} else if (this._views['callback'] != null){
            $('h2', this._views['callback']).remove();
			$('h1', this._views['callback']).after("<h2>"+l['CALL_FAILED']+"</h2>");
			this._toggleView('callback');
		} else {
			this._toggleView('intro');
		}
    }

    onCommunicatorStarted() {
        this._media = new MediaContainer((<CompatibleFactory>(this._communicator.factory)).mediaContainer, this._root);
    }

    onCommunicatorFailed(reason:string = null) {
		var msg = this._communicator.locale['COMMUNICATOR_FAILED'];
		if(reason == 'mobile' || reason == 'ie8'){
			msg = this._communicator.locale['BROWSER_COMPATIBILITY_FAILED'];
		}
		this.showCallbackForm(msg);
    }

    onSessionStarting() {}
    onSessionStarted() {}
	onSessionFailed() {}

    onIncomingCall(call:a3.Call) {}

	showCallbackForm(message:string=null) {
		$('h2', this._views['callback']).remove();
		if(message != null){
			$('h1', this._views['callback']).after("<h2>"+message+"</h2>");
		}
		this._toggleView('callback');
		$('.a3-btn-back').hide();
	}

    private _e(id:string) {
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
        this._views['talking-video']  = this._e('a3-talking-video-view');
		this._views['talking-voice']  = this._e('a3-talking-voice-view');
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

		var l = this._communicator.locale;

		/* close button */
		$('#a3').append('<div style="width:100%;position:absolute;left:0;top:0;text-align:right"><button title="'+l['CLOSE_WINDOW']+'" class="btn-close a3-btn-frame-close">&#x2715;</button></div>');

		this._initDialpad();
		this._initHelpContainer();
		this._initFooter();

		if('he' == this._communicator.query.lang){ this._root.style.direction = 'rtl'; }
        $(this._root).click((e) => {this._onClick(e)});
        this._root.style.display = 'none';

        /* send callback form */
        $(".callback-form").submit((event) => {
            event.preventDefault();
            var $form = $(event.currentTarget);
            var formdata = {};
            var trim_re = /^([*|\s]+)|([*|\s]+)$/g ;
            formdata['name'] =    $form.find("input[name='name']").val().replace(trim_re, "");
            formdata['phone'] =   $form.find("input[name='phone']").val().replace(trim_re, "");
            formdata['date'] =    $form.find("input[name='date']").val().replace(trim_re, "");
            //formdata['subject'] = $form.find("input[name='subject']").val().replace(trim_re, "");
            formdata['message'] = $form.find("textarea[name='message']").val().replace(trim_re, "");

            // simple check
            if('' == formdata['name'].trim()) return;

            // add email change request 30.04.2014
            if(0!=$form.find("input[name='email']").length){
                formdata['email'] =   $form.find("input[name='email']").val().replace(trim_re, "");
                formdata['label4email'] =   l['CALLBACK_FORM_EMAIL_LABEL'].replace(trim_re, "");
            }

            // continue check email | phone
            if('' == formdata['phone'].trim()
            &&(formdata['email'] !== undefined && '' == formdata['email'].trim())) return;

            formdata['id'] = this._communicator.query.id;
            formdata['lang'] = this._communicator.query.lang;
            //formdata.reason =         $form.find("input[name='reason']").val();

            formdata['label4name'] =    l['CALLBACK_FORM_NAME_LABEL'].replace(trim_re, "");
            formdata['label4phone'] =   l['CALLBACK_FORM_PHONE_LABEL'].replace(trim_re, "");
            formdata['label4date'] =    l['CALLBACK_FORM_DATE_LABEL'].replace(trim_re, "");
            //formdata['label4subject'] = l['CALLBACK_FORM_SUBJECT_LABEL'].replace(trim_re, "");
            formdata['label4message'] = l['CALLBACK_FORM_MESSAGE_LABEL'].replace(trim_re, "");
            
            if(CALLBACK_SERVICE == null){
                LOG(formdata);
            } else {
                this._communicator.sendStat('SUBMIT_FORM');
                $.post(CALLBACK_SERVICE, formdata)
                    .done(() => { this._toggleView('callback-result'); })
                    .fail(() => { this._toggleView('callback-result'); });
            }
        });
    }

    _rect(elem:any):any{
        var a = [0,0,0,0]; // x,y,w,h
        a[0] = parseInt(elem.style.left);
        a[1] = parseInt(elem.style.top);
        a[2] = parseInt(elem.style.width);
        a[3] = parseInt(elem.style.height);
        return a;
    }

	_initDialpad(){
		/* dialpad feature init */
		var $dialpad = $('.a3-dialpad');
		if($dialpad.length != 0){
			$('.a3-dialpad').append('<table><tr><td><button class="a3-btn-dialpad btn-gradient" data-value="1">1</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="2">2</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="3">3</button></td></tr><tr><td><button class="a3-btn-dialpad btn-gradient" data-value="4">4</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="5">5</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="6">6</button></td></tr><tr><td><button class="a3-btn-dialpad btn-gradient" data-value="7">7</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="8">8</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="9">9</button></td></tr><tr><td><button class="a3-btn-dialpad btn-gradient" data-value="*">*</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="0">0</button></td><td><button class="a3-btn-dialpad btn-gradient" data-value="#">#</button></td></tr></table><button class="btn-close a3-btn-dialpad-close">&#x2715;</button>');
			$('.a3-dialpad').hide();
		}else{
			$('.a3-btn-dialpad-toggle').remove();
		}
	}

	_initHelpContainer(){
		var h = '<div id="a3-help" data-visible="false" data-pending="false"><iframe id="a3-help-frm"></iframe></div>';
		$('body').append(h);
		this._$helpContainer = $('#a3-help');
		this._$helpContainer.hide();
	}

	_initFooter(){
		var l = this._communicator.locale;
		var copy = (typeof l['COPYRIGHT']  !== 'undefined' ? l['COPYRIGHT']  : '&copy;');
		var h = '<div class="a3-footer" style="position:absolute;width:100%;left:0;bottom:0px;"><div style="padding:4px;text-align:right;">'+copy+'</div></div>';
		$(this._root).append(h);
	}

	_initHardwareControls(){
		// TODO: hardcoded default slider value is not good!
		var h = '<table align="center" cellspacing="0"><tr>' +
			'<td style="background-image: url(\'bg-alpha.png\'); border-radius:4px"><button style="margin:4px 6px auto 6px;width:25px;height:30px;background: url(\'sprite.png\') -25px 0;border: none;" class="a3-btn-mic-mute"></button></td>' +
			'<td>&nbsp;</td>' +
			'<td style="background-image: url(\'bg-alpha.png\'); border-radius:4px 0 0 4px"><button class="a3-btn-sound-mute" style="margin-top:4px;width:30px; height:30px; background: url(\'sprite.png\') -80px 0; border: none;"></button></td>' +
			'<td style="background-image: url(\'bg-alpha.png\'); border-radius:0 4px 4px 0; padding:0 8px;"><input class="sound-volume-slider" data-slider="true" value="0.8" data-slider-highlight="true" data-slider-theme="volume" data-slider-range="0,1" data-slider-step="0.1" type="text"></td>' +
			'</tr></table>';

		$('.a3-hardware-controls').css('width', '99%').html(h);
		(<any>$(".sound-volume-slider")).simpleSlider({'theme':'volume'});
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

    _onClick(event:any){
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
            case 'btn-dialpad-toggle':
            case 'btn-dialpad-close':
                this._toggleDialpad();
                break;
            case 'btn-dialpad':
                this._communicator.onClickDialpadChar(""+$(event.target).data('value'));
                break;
			case 'btn-frame-close':
				parent.postMessage(JSON.stringify({'cmd':'close', 'id':this._communicator.query.id}), "*");
				break;

			default:
				WARN("Unhandled click on elem ." + cls);
        }
    }

    _toggleView(id:string):void
	{
        for(var p in this._views){
			if(this._views[p] == null){ continue; }
            this._views[p].style.visibility = (p == id ? 'visible' : 'hidden');
        }
		this._currentView = id;
        this._root.style.display = 'block';

		// button BACK enabled/disabled separation by views
		if(-1 != ['hw-failed', 'call-failed', 'call-finished', 'callback', 'callback-result', 'outro'].indexOf(this._currentView)){
			$('.a3-btn-back').removeAttr('disabled');
		}else{
			$('.a3-btn-back').attr('disabled','disabled');
		}

		/* start/stop stopwatch */
		var $stopwatch = $('.a3-stopwatch');
		if($stopwatch.length){
            this._stopwatchValue = 0;
            $stopwatch.html(this._toMMSS(0));
			if(-1 != this._currentView.indexOf('talking')){
				this._stopwatchIntv = setInterval(() => {
					$stopwatch.html(this._toMMSS(++this._stopwatchValue));
				}, 1000);
			}else{
				clearInterval(this._stopwatchIntv);
			}
		}

        /* dialpad feature */
		var $dialpad = $('.a3-dialpad');
		$dialpad.hide();
        var $view = $(this._views[this._currentView]);
        if($dialpad.data('dialpad') === 'show'){  // auto show on view
			$dialpad.show();
        }

        $('body').trigger('view-changed', this._currentView);

		if(this._currentView == 'callback' || this._currentView == 'call-failed'){
			(<any>$('input, textarea')).placeholder();
		}

		// if explicit height was in url string - we shoudn't resize frame
		if(this._communicator.query['h'] === undefined){
			var h = 180+$view.height();
			if(h < 256){ h = 256; }
			if(h > 600){ h = 600; }
			parent.postMessage(JSON.stringify({'cmd':'resize', 'id':this._communicator.query.id, 'w':0, 'h':h}), "*");
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
		if('true' == this._$helpContainer.attr('data-pending')){ return; }
		var isVisible:boolean = ('true' == this._$helpContainer.attr('data-visible'));
		this._$helpContainer.attr('data-pending', 'true');
		$('.a3-btn-help').attr('disabled','disabled');
		this._$helpContainer.toggle('slow', () => {
			if(!isVisible){
				this._$helpContainer.attr('data-visible', 'true');
				var helpPage:string = '/help/'+this._communicator.query.lang+'.html#'+this._currentView;
				this._$helpContainer.children(":first").attr('src', helpPage);
			}else{
				this._$helpContainer.attr('data-visible', 'false');
				this._$helpContainer.children(":first").attr('src', 'about:blank');
			}
			$('.a3-btn-help').removeAttr('disabled');
			this._$helpContainer.attr('data-pending', 'false');
		});
	}

    _toggleDialpad(){
        $('.a3-dialpad', this._views[this._currentView]).toggle();
    }
}

class ConstructorCompatibleSioSignaling extends a3.SioSignaling
{
	constructor(listener: a3.ISignalingListener, private _projectId:string, private _operatorId:string) {
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
        if(this._operatorId != null){ data['operatorId'] = this._operatorId; }
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
    private _operatorId:string = null;
	private _logLevel:string = 'NONE';

	public compatibilityError:string = null;
	public hasWRTC:boolean = false;
	public hasFlash:boolean = false;
	public mediaContainer:HTMLElement;

	constructor(){
		this.mediaContainer = document.createElement('div');
		this.mediaContainer.id = 'a3-media-container';
		document.body.appendChild(this.mediaContainer);
		if(DEBUG_ENABLED) this._logLevel = 'ALL';
	}

	addEndpoint(value:string){
		this._endpoints.push(value);
	}

	setProjectId(value:string){
		this._projectId = value;
	}

    setOperatorId(value:string){
        this._operatorId = value;
    }

	setServiceName(value:string){
		this._serviceName = value;
	}

	setListener(listener: any){
		this._listener = listener;
	}

	checkCompatibility(){
        if((<any>$).browser.mobile){
			// chrome on Android works ok!
			if(!navigator['webkitGetUserMedia'] || !window['webkitRTCPeerConnection']){
				// immediately set hardware failed if we are on a mobile platform
				this.compatibilityError = 'mobile';
				this._onWRTCCapabilityFailed();
				return;
			}
        }

		if((<any>$).browser.msie && (<any>$).browser.majorVersion <= 8){
			this.compatibilityError ='ie'+(<any>$).browser.majorVersion;
			this._onWRTCCapabilityFailed();
			return;
		}

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
			signaling = <a3.ISignaling>(new ConstructorCompatibleSioSignaling(listener, this._projectId, this._operatorId));
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
        $.support.cors = true; // http://stackoverflow.com/questions/9160123/no-transport-error-w-jquery-ajax-call-in-ie
	}

	loadTemplate(cb:any) {
		$.ajax({ url: this._tmplUrl, crossDomain : true })
			.done((data) => {
				this.tmpl = data;
				cb.call(null, this.tmpl);
			}).fail(() => { cb.call(null, false); });
	}

	loadLocale(cb:any, parse:boolean=false) {
		$.ajax({url: this._localeUrl})
			.done((data) => {
				this.locale = (parse ? parseIni(data) : data);
				cb.call(null, this.locale);
			}).fail(() => { cb.call(null, false); });
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