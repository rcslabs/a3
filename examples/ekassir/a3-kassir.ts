/// <reference path="a3.d.ts" />


class SimpleWebrtcFactory implements a3.ICommunicatorFactory {
	constructor(public sioUrl: string, private service: string = "default") {

	}
	createMedia(listener: a3.IMediaListener) {
		var element = document.getElementById('a3-video');
		return new a3.WebrtcMedia(listener, element);
	}
	createSignaling(listener: a3.ISignalingListener) {
		return new a3.SioSignaling(listener, this.sioUrl, this.service);
	}
}


var FS_ECHO = "9196";


var serviceName: string = "click2call";
var sioUrl: string = "http://192.168.1.36:16161";
var username: string = "1003";
var password: string = "1234";
var bUri: string = FS_ECHO;
var vv: boolean[] = [true, true];



class SimpleWebrtcCommunicator extends a3.Communicator {

	private isVisible: boolean = false;
	private isReady: boolean = false;

	constructor() {
		super(new SimpleWebrtcFactory(sioUrl, serviceName));
	}

	onEnterStateReady() {
		this.media.getHardwareState();
	}
	onEnterStateDisconnected() {
		this.open(username, password, "", "");
	}
	onEnterStateConnected() {
		this.isReady = true;
		var callButton: HTMLElement = document.getElementById("a3-call-button");
		callButton.className = callButton.className.replace("a3-grey-button", "a3-green-button");
	}


	toggle() {
		if(!this.isReady)
			return;

		if(!this.isVisible)
			this.show();
		else
			this.hide();
	}
	show() {
		this.isVisible = true;
		var a3App: HTMLDivElement = <HTMLDivElement>document.getElementById("a3-app");
		a3App.className = "shown";

		if(this.calls.length === 0) {
			this.startCall(bUri, vv);
		}
	}

	hide() {
		if(this.calls.length) {
			this.calls[0].hangup();
		}
		this.isVisible = false;
		var a3App: HTMLDivElement = <HTMLDivElement>document.getElementById("a3-app");
		a3App.className = "";
	}
}


(() => {
	//
	// Init
	//

	var communicator = new SimpleWebrtcCommunicator();
	communicator.start();

	document.getElementById("a3-call-button").addEventListener("click", (e) => { communicator.toggle(); }, false);
	document.getElementById("a3-hangup-button").addEventListener("click", (e) => { communicator.hide(); }, false);

})();
