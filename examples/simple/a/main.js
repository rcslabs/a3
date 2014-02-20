/// <reference path="../../cdn/a3client/a3.d.ts" />
/// <reference path="config.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};

Raphael.fn.arrow = function (x1, y1, x2, y2, size) {
    var r = this;
    var angle = Math.atan2(x1 - x2, y2 - y1);
    angle = (angle / (2 * Math.PI)) * 360;
    var arrowPath = r.path("M" + x2 + " " + y2 + " L" + (x2 - size) + " " + (y2 - size) + " L" + (x2 - size) + " " + (y2 + size) + " L" + x2 + " " + y2).attr("fill", "#444").rotate((90 + angle), x2, y2);
    var linePath = r.path("M" + x1 + " " + y1 + " L" + x2 + " " + y2).attr("color", "#ffaabb");
    return [linePath, arrowPath];
};

var CommunicatorGraph = (function () {
    function CommunicatorGraph(element) {
        var _this = this;
        this.element = element;
        this._stateElement = {};
        this._paper = Raphael(element.id);
        var r = this._paper;
        var c = "#000", f = "#ccc";
        this._stateElement[a3.State.STARTING] = r.circle(140, 20, 2).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.SIGNALING_READY] = r.circle(70, 80, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.MEDIA_READY] = r.circle(220, 80, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.FAILED] = r.circle(140, 70, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.STARTED] = r.circle(140, 120, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });

        this._stateElement[a3.State.CONNECTING] = r.circle(140, 170, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.CONNECTED] = r.circle(140, 230, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.CONNECTION_FAILED] = r.circle(220, 200, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.DISCONNECTED] = r.circle(70, 200, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });

        this._stateElement[a3.State.SESSION_STARTING] = r.circle(140, 280, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.SESSION_STARTED] = r.circle(140, 340, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });
        this._stateElement[a3.State.SESSION_FAILED] = r.circle(220, 310, 10).attr({ stroke: c, fill: f, "fill-opacity": .4 });

        for (var s in this._stateElement)
            if (this._stateElement.hasOwnProperty(s)) {
                var el = this._stateElement[s];
                var x = el.attr("cx"), y = el.attr("cy");
                r.text(x + 30, y, s);
            }
        var a = function (state1, state2) {
            var el1 = _this._stateElement[state1], el2 = _this._stateElement[state2];
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
    CommunicatorGraph.prototype._activateState = function (state) {
        for (var s in this._stateElement)
            if (this._stateElement.hasOwnProperty(s)) {
                this._stateElement[s].attr({ stroke: "#000", fill: "#ccc" });
            }
        if (!(state in this._stateElement))
            throw new Error("Unknown state " + state);
        var element = this._stateElement[state];
        element.attr({ stroke: "#ff0000", fill: "#ff0000" });
    };

    CommunicatorGraph.prototype.onCommunicatorStarting = function () {
        this._activateState(a3.State.STARTING);
    };
    CommunicatorGraph.prototype.onCommunicatorStarted = function () {
        this._activateState(a3.State.STARTED);
    };
    CommunicatorGraph.prototype.onCommunicatorFailed = function () {
    };

    CommunicatorGraph.prototype.onConnecting = function () {
        this._activateState(a3.State.CONNECTING);
    };
    CommunicatorGraph.prototype.onConnected = function () {
        this._activateState(a3.State.CONNECTED);
    };
    CommunicatorGraph.prototype.onConnectionFailed = function () {
        this._activateState(a3.State.CONNECTION_FAILED);
    };

    CommunicatorGraph.prototype.onCheckHardwareSettings = function () {
    };
    CommunicatorGraph.prototype.onCheckHardwareReady = function () {
    };
    CommunicatorGraph.prototype.onCheckHardwareFailed = function () {
    };
    CommunicatorGraph.prototype.onSoundVolumeChanged = function (value) {
    };
    CommunicatorGraph.prototype.onSessionStarting = function () {
        this._activateState(a3.State.SESSION_STARTING);
    };
    CommunicatorGraph.prototype.onSessionStarted = function () {
        this._activateState(a3.State.SESSION_STARTED);
    };
    CommunicatorGraph.prototype.onSessionFailed = function () {
        this._activateState(a3.State.SESSION_FAILED);
    };

    CommunicatorGraph.prototype.onSignalingReady = function (o) {
        this._activateState(a3.State.SIGNALING_READY);
    };
    CommunicatorGraph.prototype.onSignalingFailed = function (o) {
    };
    CommunicatorGraph.prototype.onSignalingConnected = function (o) {
    };
    CommunicatorGraph.prototype.onSignalingConnectionFailed = function (o) {
    };
    CommunicatorGraph.prototype.onMediaReady = function (opt) {
        this._activateState(a3.State.MEDIA_READY);
    };
    return CommunicatorGraph;
})();

var SimpleCommunicatorFactory = (function () {
    function SimpleCommunicatorFactory() {
    }
    SimpleCommunicatorFactory.prototype.createMedia = function (listener) {
        if (config.isWebrtcMedia())
            return new a3.WebrtcMedia(listener, document.getElementById("media-container"));
        else
            return new a3.FlashMedia(listener, document.getElementById("media-container"), {
                micRate: 44,
                micCodec: "nellymoser"
            });
    };
    SimpleCommunicatorFactory.prototype.createSignaling = function (listener) {
        if (config.isSioSignaling())
            return new a3.SioSignaling(listener);
        else
            return new a3.FlashSignaling(listener, "media-container", {});
    };
    return SimpleCommunicatorFactory;
})();

var SimpleCommunicator = (function (_super) {
    __extends(SimpleCommunicator, _super);
    function SimpleCommunicator() {
        _super.call(this);
        this._initAll = false;
        this._graph = new CommunicatorGraph(document.getElementById("communicator-graph-paper"));
    }
    SimpleCommunicator.prototype.initAll = function () {
        this._initAll = true;
        this.start();
    };
    SimpleCommunicator.prototype.start = function () {
        this.setFactory(new SimpleCommunicatorFactory());
        _super.prototype.start.call(this);
    };
    SimpleCommunicator.prototype.connect = function () {
        this.signaling.setService(config.getService());
        if (this.signaling instanceof a3.SioSignaling) {
            this.signaling.addEndpoint(config.getSioSignalingEndpoint());
        } else {
        }
        _super.prototype.connect.call(this);
    };
    SimpleCommunicator.prototype.open = function () {
        _super.prototype.open.call(this, config.getUsername(), config.getPassword(), "", "");
    };

    SimpleCommunicator.prototype.startCall = function () {
        _super.prototype.startCall.call(this, config.getBUri(), config.getVV());
    };

    SimpleCommunicator.prototype.checkHardware = function () {
        this.media.checkHardware();
    };

    // communicator subject
    SimpleCommunicator.prototype.onCommunicatorStarting = function () {
        this._graph.onCommunicatorStarting();
    };
    SimpleCommunicator.prototype.onCommunicatorStarted = function () {
        this._graph.onCommunicatorStarted();
        if (this._initAll)
            this.connect();
    };
    SimpleCommunicator.prototype.onCommunicatorFailed = function () {
        this._graph.onCommunicatorFailed();
    };

    // signaling connection
    SimpleCommunicator.prototype.onConnecting = function () {
        $("#connect-status").text("Connecting")[0].className = "working";
        this._graph.onConnecting();
    };
    SimpleCommunicator.prototype.onConnected = function () {
        $("#connect-status").text("Connected")[0].className = "done";
        this._graph.onConnected();
        if (this._initAll)
            this.open();
    };
    SimpleCommunicator.prototype.onConnectionFailed = function () {
        $("#connect-status").text("Failed")[0].className = "failed";
        this._graph.onConnectionFailed();
    };

    // hardware
    SimpleCommunicator.prototype.onCheckHardwareSettings = function () {
        $("#hardware-status").text("checking")[0].className = "working";
        this._graph.onCheckHardwareSettings();
    };
    SimpleCommunicator.prototype.onCheckHardwareReady = function () {
        $("#hardware-status").text("ready")[0].className = "done";
        this._graph.onCheckHardwareReady();
    };
    SimpleCommunicator.prototype.onCheckHardwareFailed = function () {
        $("#hardware-status").text("failed")[0].className = "failed";
        this._graph.onCheckHardwareFailed();
    };
    SimpleCommunicator.prototype.onSoundVolumeChanged = function (value) {
        this._graph.onSoundVolumeChanged(value);
    };

    // session
    SimpleCommunicator.prototype.onSessionStarting = function () {
        $("#session-status").text("Session starting")[0].className = "working";
        this._graph.onSessionStarting();
    };
    SimpleCommunicator.prototype.onSessionStarted = function () {
        $("#session-status").text("Session started")[0].className = "done";
        this._graph.onSessionStarted();
        if (this._initAll)
            this.checkHardware();
    };
    SimpleCommunicator.prototype.onSessionFailed = function () {
        $("#session-status").text("Session failed")[0].className = "failed";
        this._graph.onSessionFailed();
    };

    SimpleCommunicator.prototype.onSignalingReady = function (o) {
        $("#signaling-status").text("Signaling ready")[0].className = "done";
        this._graph.onSignalingReady(o);
        _super.prototype.onSignalingReady.call(this, o);
    };

    SimpleCommunicator.prototype.onSignalingFailed = function (o) {
        $("#signaling-status").text("Signaling failed")[0].className = "failed";
        this._graph.onSignalingFailed(o);
        _super.prototype.onSignalingFailed.call(this, o);
    };

    SimpleCommunicator.prototype.onSignalingConnected = function (o) {
        console.log("onSignalingConnected");
        this._graph.onSignalingConnected(o);
        _super.prototype.onSignalingConnected.call(this, o);
    };

    SimpleCommunicator.prototype.onSignalingConnectionFailed = function (o) {
        this._graph.onSignalingConnectionFailed(o);
        _super.prototype.onSignalingConnectionFailed.call(this, o);
    };
    SimpleCommunicator.prototype.onMediaReady = function (opt) {
        $("#media-status").text("Media ready")[0].className = "done";
        this._graph.onMediaReady(opt);
        _super.prototype.onMediaReady.call(this, opt);
    };
    return SimpleCommunicator;
})(a3.Communicator);

var communicator = new SimpleCommunicator();

$("#init-all").click(function () {
    communicator.initAll();
});
$("#start").click(function () {
    communicator.start();
});
$("#connect").click(function () {
    communicator.connect();
});
$("#disconnect").click(function () {
    alert("Not implemented");
});
$("#start-session").click(function () {
    communicator.open();
});
$("#stop-session").click(function () {
    alert("Not implemented");
});
$("#start-call").click(function () {
    communicator.startCall();
});
$("#check-hardware").click(function () {
    communicator.checkHardware();
});

if (config.isInitAutomatically()) {
    $("#init-all").click();
}

$("#communicator-graph-mark").click(function () {
    $("#communicator-graph").toggleClass("hidden");
    config.applyData();
    config.save();
});
//# sourceMappingURL=main.js.map
