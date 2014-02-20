/// <reference path="../../cdn/jquery.d.ts" />
/// <reference path="../../cdn/config/config.ts" />
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
//
// MediaTester config
//
var Config = (function (_super) {
    __extends(Config, _super);
    function Config() {
        _super.call(this, "config-media-tester");
    }
    Config.prototype.defaults = function () {
        return {
            "pub-audio": "rtmp://192.168.1.200/live/subaudio",
            "sub-audio": "rtmp://192.168.1.200/live/pubaudio",
            "sub-video": "rtmp://192.168.1.200/live/pubvideo",
            "pub-video": "rtmp://192.168.1.200/live/subvideo",
            "is-pub-audio": true,
            "is-sub-audio": true,
            "is-pub-video": false,
            "is-sub-video": false
        };
    };

    //
    // getters
    //
    Config.prototype.getPubAudio = function () {
        return this.value("is-pub-audio") ? this.value("pub-audio") : null;
    };
    Config.prototype.getSubAudio = function () {
        return this.value("is-sub-audio") ? this.value("sub-audio") : null;
    };
    Config.prototype.getPubVideo = function () {
        return this.value("is-pub-video") ? this.value("pub-video") : null;
    };
    Config.prototype.getSubVideo = function () {
        return this.value("is-sub-video") ? this.value("sub-video") : null;
    };

    Config.prototype.getFlashVars = function () {
        var result = {};
        this.each(function (key, value) {
            if (key.indexOf("fv-") === 0 && value) {
                result[key.substr(3)] = value;
            }
        });
        return result;
    };
    return Config;
})(BaseConfig);

var config = new Config();
//# sourceMappingURL=config.js.map
