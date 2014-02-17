/// <reference path="jquery.d.ts" />
var $config = $(".config");

var Config = (function () {
    function Config() {
        this._data = {
            "sio-signaling": true,
            "webrtc-media": true
        };
        this.load();
        this.applyUi();
        this.addListeners();
    }
    Config.prototype.load = function () {
        if (!window.localStorage)
            return;
        var storage = window.localStorage;
        $.extend(this._data, JSON.parse(storage.getItem("config-data")));
    };
    Config.prototype.save = function () {
        if (!window.localStorage)
            throw new Error("No window.localStorage");
        var storage = window.localStorage;
        storage.setItem("config-data", JSON.stringify(this._data));
    };
    Config.prototype.addListeners = function () {
        var _this = this;
        $("input[data-config], textarea[data-config]").on("change keypress", function () {
            console.log("Config changed");
            _this.applyData();
            _this.save();
        });
    };

    Config.prototype.applyUi = function () {
        var _this = this;
        $("[data-config]").each(function (_, el) {
            var key = $(el).data("config"), $el = $(el);
            if (!(key in _this._data)) {
                console.warn("Config: Error in data-config: key=" + key);
                return;
            }
            var value = _this._data[key];
            if (el.tagName.toLowerCase() === "input" && $el.attr("type") === "radio") {
                $el.prop("checked", value);
            } else {
                $el.val(value);
            }
        });
    };
    Config.prototype.applyData = function () {
        var _this = this;
        $("[data-config]").each(function (_, el) {
            var key = $(el).data("config"), $el = $(el);
            var value;
            if (el.tagName.toLowerCase() === "input" && $el.attr("type") === "radio") {
                value = $el.prop("checked");
            } else {
                value = $el.val();
            }
            _this._data[key] = value;
        });
    };

    Config.prototype.isSioSignaling = function () {
        return !!this._data["sio-signaling"];
    };
    Config.prototype.isFlashSignaling = function () {
        return !this.isSioSignaling();
    };
    Config.prototype.isWebrtcMedia = function () {
        return !!this._data["webrtc-media"];
    };
    Config.prototype.isFlashMedia = function () {
        return !this.isWebrtcMedia();
    };
    Config.prototype.getSioSignalingEndpoint = function () {
        return this._data["sio-signaling-endpoint"];
    };
    Config.prototype.getUsername = function () {
        return this._data["username"];
    };
    Config.prototype.getPassword = function () {
        return this._data["password"];
    };
    Config.prototype.getService = function () {
        return this._data["service"];
    };
    return Config;
})();

var config = new Config();
//# sourceMappingURL=config.js.map
