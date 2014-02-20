/// <reference path="a3.d.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
var SimpleWebrtcFactory = (function () {
    function SimpleWebrtcFactory(sioUrl, service) {
        if (typeof service === "undefined") { service = "default"; }
        this.sioUrl = sioUrl;
        this.service = service;
    }
    SimpleWebrtcFactory.prototype.createMedia = function (listener) {
        var element = document.getElementById('a3-video');
        return new a3.WebrtcMedia(listener, element);
    };
    SimpleWebrtcFactory.prototype.createSignaling = function (listener) {
        return new a3.SioSignaling(listener, this.sioUrl, this.service);
    };
    return SimpleWebrtcFactory;
})();

var FS_ECHO = "9196";

var serviceName = "click2call";
var sioUrl = "http://192.168.1.36:16161";
var username = "1003";
var password = "1234";
var bUri = FS_ECHO;
var vv = [true, true];

var SimpleWebrtcCommunicator = (function (_super) {
    __extends(SimpleWebrtcCommunicator, _super);
    function SimpleWebrtcCommunicator() {
        _super.call(this, new SimpleWebrtcFactory(sioUrl, serviceName));
        this.isVisible = false;
        this.isReady = false;
    }
    SimpleWebrtcCommunicator.prototype.onEnterStateReady = function () {
        this.media.getHardwareState();
    };
    SimpleWebrtcCommunicator.prototype.onEnterStateDisconnected = function () {
        this.open(username, password, "", "");
    };
    SimpleWebrtcCommunicator.prototype.onEnterStateConnected = function () {
        this.isReady = true;
        var callButton = document.getElementById("a3-call-button");
        callButton.className = callButton.className.replace("a3-grey-button", "a3-green-button");
    };

    SimpleWebrtcCommunicator.prototype.toggle = function () {
        if (!this.isReady)
            return;

        if (!this.isVisible)
            this.show();
        else
            this.hide();
    };
    SimpleWebrtcCommunicator.prototype.show = function () {
        this.isVisible = true;
        var a3App = document.getElementById("a3-app");
        a3App.className = "shown";

        if (this.calls.length === 0) {
            this.startCall(bUri, vv);
        }
    };

    SimpleWebrtcCommunicator.prototype.hide = function () {
        if (this.calls.length) {
            this.calls[0].hangup();
        }
        this.isVisible = false;
        var a3App = document.getElementById("a3-app");
        a3App.className = "";
    };
    return SimpleWebrtcCommunicator;
})(a3.Communicator);

(function () {
    //
    // Init
    //
    var communicator = new SimpleWebrtcCommunicator();
    communicator.start();

    document.getElementById("a3-call-button").addEventListener("click", function (e) {
        communicator.toggle();
    }, false);
    document.getElementById("a3-hangup-button").addEventListener("click", function (e) {
        communicator.hide();
    }, false);
})();
//# sourceMappingURL=a3-kassir.js.map
