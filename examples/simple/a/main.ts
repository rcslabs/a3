/// <reference path="../../cdn/a3client/a3.d.ts" />
/// <reference path="config.ts" />

declare var Raphael;

Raphael.fn.arrow = function (x1, y1, x2, y2, size) {
	var r = <any>this;
	var angle = Math.atan2(x1-x2,y2-y1);
	angle = (angle / (2 * Math.PI)) * 360;
	var arrowPath = r.path("M" + x2 + " " + y2 + " L" + (x2 - size) + " " + (y2 - size) + " L" + (x2 - size) + " " + (y2 + size) + " L" + x2 + " " + y2 )
	                 .attr("fill","#444").rotate((90+angle),x2,y2);
	var linePath = r.path("M" + x1 + " " + y1 + " L" + x2 + " " + y2).attr("color", "#ffaabb");
	return [linePath,arrowPath];
};

class CommunicatorGraph implements a3.ICommunicatorListener{
	private _paper: any;
	private _stateElement: any = {};
	constructor(private element: HTMLElement) {
		this._paper = Raphael(element.id);
		var r = this._paper;
		var c = "#000", f="#ccc";
		this._stateElement[a3.State.STARTING]          = r.circle(140, 20,	2).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.SIGNALING_READY]   = r.circle( 70, 80, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.MEDIA_READY]       = r.circle(220, 80, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.FAILED]            = r.circle(140, 70, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.STARTED]           = r.circle(140,120, 10).attr({stroke: c, fill: f, "fill-opacity": .4});

		this._stateElement[a3.State.CONNECTING]        = r.circle(140,170, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.CONNECTED]         = r.circle(140,230, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.CONNECTION_FAILED] = r.circle(220,200, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.DISCONNECTED]      = r.circle( 70,200, 10).attr({stroke: c, fill: f, "fill-opacity": .4});

		this._stateElement[a3.State.SESSION_STARTING]  = r.circle(140,280, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.SESSION_STARTED]   = r.circle(140,340, 10).attr({stroke: c, fill: f, "fill-opacity": .4});
		this._stateElement[a3.State.SESSION_FAILED]    = r.circle(220,310, 10).attr({stroke: c, fill: f, "fill-opacity": .4});

		for(var s in this._stateElement) if(this._stateElement.hasOwnProperty(s)) {
			var el = this._stateElement[s];
			var x = el.attr("cx"), y = el.attr("cy");
			r.text(x+30, y, s);
		}
		var a = (state1, state2) => {
			var el1 = this._stateElement[state1], el2 = this._stateElement[state2];
			r.arrow(el1.attr("cx"), el1.attr("cy"), el2.attr("cx"), el2.attr("cy"), 8);
		};
		a(a3.State.STARTING, a3.State.SIGNALING_READY);
		a(a3.State.STARTING, a3.State.MEDIA_READY);
		a(a3.State.SIGNALING_READY, a3.State.STARTED);
		a(a3.State.MEDIA_READY, a3.State.STARTED);
		a(a3.State.STARTED, a3.State.CONNECTING);
		a(a3.State.CONNECTING, a3.State.CONNECTED);
		a(a3.State.SESSION_STARTING, a3.State.SESSION_FAILED);
		a(a3.State.SESSION_STARTING, a3.State.SESSION_STARTED);
	}


	private _activateState(state: string) {
		for(var s in this._stateElement) if(this._stateElement.hasOwnProperty(s)) {
			this._stateElement[s].attr({stroke: "#000", fill: "#ccc"})
		}
		if(!(state in this._stateElement)) throw new Error("Unknown state " + state);
		var element = this._stateElement[state];
		element.attr({stroke: "#ff0000", fill: "#ff0000"})
	}

	onCommunicatorStarting() { this._activateState(a3.State.STARTING);}
	onCommunicatorStarted() { this._activateState(a3.State.STARTED); }
	onCommunicatorFailed() {}

	onConnecting() { this._activateState(a3.State.CONNECTING); }
	onConnected() { this._activateState(a3.State.CONNECTED); }
	onConnectionFailed() { this._activateState(a3.State.CONNECTION_FAILED); }

	onCheckHardwareSettings() {}
	onCheckHardwareReady() {}
	onCheckHardwareFailed() {}
	onSoundVolumeChanged(value:number) {}
	onSessionStarting() { this._activateState(a3.State.SESSION_STARTING); }
	onSessionStarted() { this._activateState(a3.State.SESSION_STARTED); }
	onSessionFailed() { this._activateState(a3.State.SESSION_FAILED); }

	onSignalingReady(o: a3.ISignaling) { this._activateState(a3.State.SIGNALING_READY); }
	onSignalingFailed(o: a3.ISignaling) {	}
	onSignalingConnected(o:a3.ISignaling) {	}
	onSignalingConnectionFailed(o:a3.ISignaling) { }
	onMediaReady(opt) { this._activateState(a3.State.MEDIA_READY); }
}


var SCALE = 2;

var flashVars = {
	micRate: 44,
	micCodec: "nellymoser",
	logLevel: "ALL",
	camCodec: "h264",
	camWidth: 352 * SCALE,
	camHeight: 288 * SCALE,
	checkMicVolume: false
};

class SimpleCommunicatorFactory implements a3.ICommunicatorFactory {
	createMedia(listener: a3.IMediaListener): a3.IMedia {
		if(config.isWebrtcMedia())
			return new a3.WebrtcMedia(listener, document.getElementById("media"));
		else
			return new a3.FlashMedia(listener, document.getElementById("media"), flashVars);
	}
	createSignaling(listener: a3.ISignalingListener): a3.ISignaling {
		if(config.isSioSignaling())
			return new a3.SioSignaling(listener);
		else
			return new a3.FlashSignaling(listener, "flash-signaling-container", {});
	}
}


class SimpleCommunicator extends a3.Communicator {
	private _graph: CommunicatorGraph;
	private _initAll: boolean = false;

	constructor() {
		super();
		this._graph = new CommunicatorGraph(document.getElementById("communicator-graph-paper"));
	}
	initAll() {
		this._initAll = true;
		this.start();
	}
	start() {
		this.setFactory(new SimpleCommunicatorFactory());
		super.start();
	}
	connect() {
		this.signaling.setService(config.getService());
		if(this.signaling instanceof a3.SioSignaling) {
			this.signaling.addEndpoint(config.getSioSignalingEndpoint());
		} else {

		}
		super.connect();
	}
	open() {
		super.open(config.getUsername(), config.getPassword(), "", "");
	}

	startCall() {
		super.startCall(config.getBUri(), config.getVV());
	}

	checkHardware() {
		this.media.checkHardware([true, true]);
	}




	// communicator subject
	onCommunicatorStarting() { this._graph.onCommunicatorStarting(); }
	onCommunicatorStarted() {
		this._graph.onCommunicatorStarted();
		if(this._initAll) this.connect();
	}
	onCommunicatorFailed() { this._graph.onCommunicatorFailed(); }

	// signaling connection
	onConnecting() {
		$("#connect-status").text("Connecting")[0].className = "working";
		this._graph.onConnecting();
	}
	onConnected() {
		$("#connect-status").text("Connected")[0].className = "done";
		this._graph.onConnected();
		if(this._initAll) this.open();
	}
	onConnectionFailed() {
		$("#connect-status").text("Failed")[0].className = "failed";
		this._graph.onConnectionFailed();
	}

	// hardware
	onCheckHardwareSettings() {
		console.log("onCheckHardwareSettings");




		$("#hardware-status").text("checking")[0].className = "working";
		this._graph.onCheckHardwareSettings();
	}
	onCheckHardwareReady() {
		console.log("onCheckHardwareReady");
		$("#hardware-status").text("ready")[0].className = "done";
		this._graph.onCheckHardwareReady();
	}
	onCheckHardwareFailed() {
		$("#hardware-status").text("failed")[0].className = "failed";
		this._graph.onCheckHardwareFailed();
	}
	onSoundVolumeChanged(value:number) { this._graph.onSoundVolumeChanged(value); }

	// session
	onSessionStarting() {
		$("#session-status").text("Session starting")[0].className = "working";
		this._graph.onSessionStarting();
	}
	onSessionStarted() {
		$("#session-status").text("Session started")[0].className = "done";
		this._graph.onSessionStarted();
		if(this._initAll) this.checkHardware();
	}
	onSessionFailed() {
		$("#session-status").text("Session failed")[0].className = "failed";
		this._graph.onSessionFailed();
	}


	onSignalingReady(o: a3.ISignaling) {
		$("#signaling-status").text("Signaling ready")[0].className = "done";
		this._graph.onSignalingReady(o);
		super.onSignalingReady(o);
	}

	onSignalingFailed(o: a3.ISignaling) {
		$("#signaling-status").text("Signaling failed")[0].className = "failed";
		this._graph.onSignalingFailed(o);
		super.onSignalingFailed(o);
	}

	onSignalingConnected(o:a3.ISignaling) {
		console.log("onSignalingConnected");
		this._graph.onSignalingConnected(o);
		super.onSignalingConnected(o);
	}

	onSignalingConnectionFailed(o:a3.ISignaling) {
		this._graph.onSignalingConnectionFailed(o);
		super.onSignalingConnectionFailed(o);
	}
	onMediaReady(opt) {
		$("#media-status").text("Media ready")[0].className = "done";
		this._graph.onMediaReady(opt);
		super.onMediaReady(opt);
	}


}


var communicator = new SimpleCommunicator();


$("#init-all").click(() => { communicator.initAll(); });
$("#start").click(() => { communicator.start(); });
$("#connect").click(() => { communicator.connect(); });
$("#disconnect").click(() => { alert("Not implemented")});
$("#start-session").click(() => { communicator.open(); });
$("#stop-session").click(() => { alert("Not implemented") });
$("#start-call").click(() => { communicator.startCall(); });
$("#check-hardware").click(() => { communicator.checkHardware(); });

if(config.isInitAutomatically()) {
	communicator.initAll();
}

$("#communicator-graph-mark").click(() => {
	$("#communicator-graph").toggleClass("hidden");
	config.apply();
});