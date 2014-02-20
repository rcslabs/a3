/// <reference path="../../cdn/jquery.d.ts" />
/// <reference path="../../cdn/config/config.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
//
// Simple: config
//
var Config = (function (_super) {
    __extends(Config, _super);
    function Config() {
        _super.call(this, "config-simple");
    }
    Config.prototype.defaults = function () {
        return {
            "sio-signaling": true,
            "webrtc-media": true,
            "service": "click2call"
        };
    };

    //
    // getters
    //
    Config.prototype.isSioSignaling = function () {
        return !!this.value("sio-signaling");
    };
    Config.prototype.isFlashSignaling = function () {
        return !this.isSioSignaling();
    };
    Config.prototype.isWebrtcMedia = function () {
        return !!this.value("webrtc-media");
    };
    Config.prototype.isFlashMedia = function () {
        return !this.isWebrtcMedia();
    };
    Config.prototype.getSioSignalingEndpoint = function () {
        return this.value("sio-signaling-endpoint");
    };
    Config.prototype.getUsername = function () {
        return this.value("username");
    };
    Config.prototype.getPassword = function () {
        return this.value("password");
    };
    Config.prototype.getService = function () {
        return this.value("service");
    };
    Config.prototype.getBUri = function () {
        return this.value("b-uri");
    };
    Config.prototype.getVV = function () {
        return [!!this.value("vv-voice"), !!this.value("vv-video")];
    };
    Config.prototype.isInitAutomatically = function () {
        return !!this.value("init-automatically");
    };
    return Config;
})(BaseConfig);

var config = new Config();
//# sourceMappingURL=config.js.map
