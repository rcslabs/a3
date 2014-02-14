/// <reference path="qunit-1.10.0.d.ts" />
/// <reference path="../src/communicator.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
var a3test;
(function (a3test) {
    var Signaling = (function () {
        function Signaling(listener, _id) {
            this.listener = listener;
            this._id = _id;
        }
        Signaling.prototype.start = function () {
            var _this = this;
            console.log("Signaling::start");
            window.setTimeout(function () {
                _this.listener.onSignalingReady(_this);
            }, 50);
        };

        Signaling.prototype.setClientInfo = function (prop, value) {
        };
        Signaling.prototype.open = function (username, password, challenge, code) {
            var _this = this;
            console.log("Signaling::open");
            this._notify("SESSION_STARTING", { sessionId: this._id });
            window.setTimeout(function () {
                if (username === "username" && password === "password") {
                    _this._notify("SESSION_STARTED", { sessionId: _this._id });
                } else {
                    _this._notify("SESSION_FAILED", { sessionId: _this._id });
                }
            }, 100);
        };
        Signaling.prototype.startCall = function (bUri, cc, vv) {
        };
        Signaling.prototype.hangup = function (callId) {
        };
        Signaling.prototype.sdpAnswer = function (callId, pointId, sdp) {
        };
        Signaling.prototype.dtmf = function (callId, dtmf) {
        };

        Signaling.prototype.request = function (type, opt) {
            // ...
        };
        Signaling.prototype._notifyReady = function () {
            if (this.listener)
                this.listener.onSignalingReady(this);
        };
        Signaling.prototype._notify = function (type, opt) {
            if (this.listener)
                this.listener.onSignalingMessage(type, opt);
        };
        return Signaling;
    })();
    a3test.Signaling = Signaling;

    var Media = (function () {
        function Media(_listener, _id) {
            var _this = this;
            this._listener = _listener;
            this._id = _id;
            window.setTimeout(function () {
                _this._listener.onMediaReady(_this);
            }, 50);
        }
        Media.prototype.getCc = function () {
            return {
                test: true,
                id: this._id
            };
        };
        Media.prototype.getHardwareState = function () {
            var _this = this;
            console.log("Media::getHardwareState");
            this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", {
                data: { microphone: { state: "disabled" }, camera: { state: "disabled" } }
            });

            window.setTimeout(function () {
                _this._listener.onMediaMessage("HardwareEvent.HARDWARE_STATE", {
                    data: { microphone: { state: "enabled" }, camera: { state: "enabled" } }
                });
            }, 100);
        };
        Media.prototype.setMicrophoneVolume = function (volume) {
            //
        };
        Media.prototype.setSoundVolume = function (volume) {
            //
        };
        Media.prototype.setOfferSdp = function (callId, pointId, offerSdp) {
            //
        };
        Media.prototype.playDtmf = function (dtmf) {
            //
        };
        return Media;
    })();

    var Factory = (function () {
        function Factory(_id) {
            this._id = _id;
        }
        Factory.prototype.createMedia = function (listener) {
            return new Media(listener, this._id);
        };
        Factory.prototype.createSignaling = function (listener) {
            return new Signaling(listener, this._id);
        };
        return Factory;
    })();
    a3test.Factory = Factory;

    var Communicator = (function (_super) {
        __extends(Communicator, _super);
        function Communicator(factory, _override) {
            _super.call(this, factory);
            this._override = _override;
        }
        Communicator.prototype.onEnterStateStarting = function () {
            this.callOverride("onEnterStateStarting");
        };
        Communicator.prototype.onEnterStateMediaReady = function () {
            this.callOverride("onEnterStateMediaReady");
        };
        Communicator.prototype.onEnterStateSignalingReady = function () {
            this.callOverride("onEnterStateSignalingReady");
        };
        Communicator.prototype.onEnterStateReady = function () {
            this.callOverride("onEnterStateReady");
        };
        Communicator.prototype.onEnterStateHardwareSettings = function () {
            this.callOverride("onEnterStateHardwareSettings");
        };
        Communicator.prototype.onEnterStateDisconnected = function () {
            this.callOverride("onEnterStateDisconnected");
        };
        Communicator.prototype.onEnterStateConnecting = function () {
            this.callOverride("onEnterStateConnecting");
        };
        Communicator.prototype.onEnterStateConnected = function () {
            this.callOverride("onEnterStateConnected");
        };
        Communicator.prototype.onIncomingCall = function (call) {
            this.callOverride("onIncomingCall", call);
        };

        Communicator.prototype.callOverride = function (functionName) {
            var args = [];
            for (var _i = 0; _i < (arguments.length - 1); _i++) {
                args[_i] = arguments[_i + 1];
            }
            if (this._override && (functionName in this._override) && (typeof this._override[functionName] === "function")) {
                this._override[functionName].apply(this, args);
            }
        };
        return Communicator;
    })(a3.Communicator);
    a3test.Communicator = Communicator;
})(a3test || (a3test = {}));

//
//
//  Tests
//
//
QUnit.asyncTest("INIT TEST", 2, function () {
    var isReady = false, isDisconnected = false;

    var communicator = new a3test.Communicator(new a3test.Factory("INIT TEST"), {
        onEnterStateReady: function () {
            ok(isReady === false && isDisconnected === false, "onEnterStateReady in wrong order: Ready=" + isReady + ", Disconnected=" + isDisconnected);
            isReady = true;
            this.media.getHardwareState();
        },
        onEnterStateDisconnected: function () {
            ok(isReady === true && isDisconnected === false, "onEnterStateDisconnected in wrong order Ready=" + isReady + ", Disconnected=" + isDisconnected);
            isDisconnected = true;

            //this.open("username", "password", "", "");
            start();
        }
    });
    communicator.start();
});

QUnit.asyncTest("INCOMING CALL TEST", 1, function () {
    var communicator = new a3test.Communicator(new a3test.Factory("INIT TEST"), {
        onEnterStateReady: function () {
            this.media.getHardwareState();
        },
        onEnterStateDisconnected: function () {
            this.open("username", "password", "", "");
        },
        onEnterStateConnected: function () {
            this.signaling._notify("INCOMING_CALL", { vv: [true, true] });
        },
        onIncomingCall: function (call) {
            ok(communicator.calls.length === 1, "Incoming call registered");
            window.setTimeout(function () {
                call.accept();
                start();
            }, 100);
        }
    });
    communicator.start();
});

QUnit.asyncTest("INCOMING CALL DECLINE", 2, function () {
    var webrtcMedia = new a3.WebrtcMedia(null, document.body);

    var communicator = new a3test.Communicator(new a3test.Factory("INIT TEST"), {
        onEnterStateReady: function () {
            this.media.getHardwareState();
        },
        onEnterStateDisconnected: function () {
            this.open("username", "password", "", "");
        },
        onEnterStateConnected: function () {
            this.signaling._notify("INCOMING_CALL", { vv: [true, true] });
        },
        onIncomingCall: function (call) {
            ok(communicator.calls.length === 1, "Incoming call registered");

            window.setTimeout(function () {
                call.decline();
                ok(communicator.calls.length === 0, "Deciled calls exists");
                start();

                webrtcMedia.playDtmf("7 (911) 253-07-21");
            }, 1500);
        }
    });
    communicator.start();
});
//# sourceMappingURL=tests.js.map
