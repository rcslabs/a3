/// <reference path="qunit-1.10.0.d.ts" />
/// <reference path="../src/communicator.ts" />

module a3test {
    export class Signaling implements a3.ISignaling {
        constructor(private listener, private _id) {
        }

        start() {
            console.log("Signaling::start");
            window.setTimeout(() =>{
                this.listener.onSignalingReady(this);
            }, 50);
        }

        setClientInfo(prop: string, value: string) {}
        open(username: string, password: string, challenge: string, code: string) {
            console.log("Signaling::open");
            this._notify("SESSION_STARTING", {sessionId: this._id});
            window.setTimeout(() => {
                if(username === "username" && password === "password") {
                    this._notify("SESSION_STARTED", {sessionId: this._id});
                } else {
                    this._notify("SESSION_FAILED", {sessionId: this._id});
                }
            }, 100);
        }
        startCall(bUri: string, cc: Object, vv: boolean[]) {}
        hangup(callId: string) {}
        sdpAnswer(callId: string, pointId: string, sdp: any) {}
        dtmf(callId: string, dtmf: string) {}


	    request(type: string, opt: Object) {
		    // ...
	    }
	    _notifyReady() {
		    if(this.listener)
			    this.listener.onSignalingReady(this)
	    }
	    _notify(type: string, opt: any) {
		    if(this.listener)
			    this.listener.onSignalingMessage(type, opt);
	    }
    }


    class Media implements a3.IMedia {
        constructor(private _listener: a3.IMediaListener, private _id: string)  {
            window.setTimeout(() => {
                this._listener.onMediaReady(this);
            }, 50);
        }
        getCc(): any {
            return {
                test: true,
                id: this._id
            }
        }
        getHardwareState() {
            console.log("Media::getHardwareState");
            this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", {
                data: { microphone: { state: "disabled"}, camera: { state: "disabled"} }
            });

            window.setTimeout(() => {
                this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", {
                    data: { microphone: { state: "enabled" }, camera: { state: "enabled" } }
                });
            }, 100);
        }
        setMicrophoneVolume(volume: number) {
            //
        }
        setSoundVolume(volume: number) {
            //
        }
        setOfferSdp(callId: string, pointId: string, offerSdp: any) {
            //
        }
	    playDtmf(dtmf: string) {
		    //
	    }
    }


    export class Factory implements a3.ICommunicatorFactory {
        constructor(private _id: string) {}

        createMedia(listener: a3.IMediaListener): a3.IMedia {
            return new Media(listener, this._id)
        }
        createSignaling(listener: a3.ISignalingListener): a3.ISignaling {
            return new Signaling(listener, this._id);
        }
    }

    export class Communicator extends a3.Communicator {
        constructor(factory: a3.ICommunicatorFactory, private _override: any) {
            super(factory)
        }

        onEnterStateStarting() { this.callOverride("onEnterStateStarting") }
        onEnterStateMediaReady() { this.callOverride("onEnterStateMediaReady") }
        onEnterStateSignalingReady() { this.callOverride("onEnterStateSignalingReady") }
        onEnterStateReady() { this.callOverride("onEnterStateReady") }
        onEnterStateHardwareSettings() { this.callOverride("onEnterStateHardwareSettings") }
        onEnterStateDisconnected() { this.callOverride("onEnterStateDisconnected") }
        onEnterStateConnecting() { this.callOverride("onEnterStateConnecting") }
        onEnterStateConnected() { this.callOverride("onEnterStateConnected") }
	    onIncomingCall(call: a3.Call) {  this.callOverride("onIncomingCall", call) }


        callOverride(functionName: string, ...args : any[]) {
            if(this._override && (functionName in this._override) && (typeof this._override[functionName] === "function")) {
                this._override[functionName].apply(this, args);
            }
        }
    }
}

//
//
//  Tests
//
//

QUnit.asyncTest("INIT TEST", 2, () => {

    var isReady: boolean = false, isDisconnected: boolean = false;

    var communicator = new a3test.Communicator(
        new a3test.Factory("INIT TEST"),
        {
            onEnterStateReady: function() {
                ok(isReady === false && isDisconnected === false, "onEnterStateReady in wrong order: Ready=" + isReady +  ", Disconnected=" + isDisconnected);
                isReady = true;
                this.media.getHardwareState();
            },
            onEnterStateDisconnected: function() {
                ok(isReady === true && isDisconnected === false, "onEnterStateDisconnected in wrong order Ready=" + isReady +  ", Disconnected=" + isDisconnected);
                isDisconnected = true;
                //this.open("username", "password", "", "");
                start();
            }
        });
    communicator.start();
});



QUnit.asyncTest("INCOMING CALL TEST", 1, () => {
    var communicator = new a3test.Communicator(
        new a3test.Factory("INIT TEST"),
        {
            onEnterStateReady: function() { this.media.getHardwareState(); },
            onEnterStateDisconnected: function() { this.open("username", "password", "", ""); },
            onEnterStateConnected: function() {
                this.signaling._notify("INCOMING_CALL", { vv: [true, true]});
            },
	        onIncomingCall: function(call: a3.Call) {
		        ok(communicator.calls.length === 1, "Incoming call registered");
		        window.setTimeout(function() {
			        call.accept();
			        start();
		        }, 100);
	        }
        });
    communicator.start();
});



QUnit.asyncTest("INCOMING CALL DECLINE", 2, () => {


	var webrtcMedia = new a3.WebrtcMedia(null, document.body);

	var communicator = new a3test.Communicator(
		new a3test.Factory("INIT TEST"),
		{
			onEnterStateReady: function() { this.media.getHardwareState(); },
			onEnterStateDisconnected: function() { this.open("username", "password", "", ""); },
			onEnterStateConnected: function() {
				this.signaling._notify("INCOMING_CALL", { vv: [true, true]});
			},
			onIncomingCall: function(call: a3.Call) {
				ok(communicator.calls.length === 1, "Incoming call registered");

				window.setTimeout(function() {
					call.decline();
					ok(communicator.calls.length === 0, "Deciled calls exists");
					start();

					webrtcMedia.playDtmf("7 (911) 253-07-21");
				}, 1500);
			}
		});
	communicator.start();
});
