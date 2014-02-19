/// <reference path="jquery.d.ts" />
var $config = $(".config");

var Config = (function () {
    function Config() {
        this._data = {
            "pub-audio": "rtmp://192.168.1.200/live/subaudio",
            "sub-audio": "rtmp://192.168.1.200/live/pubaudio",
            "sub-video": "rtmp://192.168.1.200/live/pubvideo",
            "pub-video": "rtmp://192.168.1.200/live/subvideo",
            "is-pub-audio": true,
            "is-sub-audio": true,
            "is-pub-video": false,
            "is-sub-video": false
        };
        this.load();
        this.applyUi();
        this.addListeners();
    }
    Config.prototype.load = function () {
        if (!window.localStorage)
            return;
        var storage = window.localStorage;
        $.extend(this._data, JSON.parse(storage.getItem("config-media")));
    };
    Config.prototype.save = function () {
        if (!window.localStorage)
            throw new Error("No window.localStorage");
        var storage = window.localStorage;
        storage.setItem("config-media", JSON.stringify(this._data));
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
            if (el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
                $el.prop("checked", value);
            } else if (el.tagName.toLowerCase() === "input" || el.tagName.toLowerCase() === "textarea") {
                $el.val(value);
            } else {
                $el.get(0).className = value;
            }
        });
    };
    Config.prototype.applyData = function () {
        var _this = this;
        $("[data-config]").each(function (_, el) {
            var key = $(el).data("config"), $el = $(el);
            var value;
            if (el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
                value = $el.prop("checked");
            } else if (el.tagName.toLowerCase() === "input" || el.tagName.toLowerCase() === "textarea") {
                value = $el.val();
            } else {
                value = $el.get(0).className;
            }
            _this._data[key] = value;
        });
    };

    Config.prototype.getPubAudio = function () {
        return this._data["is-pub-audio"] ? this._data["pub-audio"] : null;
    };
    Config.prototype.getSubAudio = function () {
        return this._data["is-sub-audio"] ? this._data["sub-audio"] : null;
    };
    Config.prototype.getPubVideo = function () {
        return this._data["is-pub-video"] ? this._data["pub-video"] : null;
    };
    Config.prototype.getSubVideo = function () {
        return this._data["is-sub-video"] ? this._data["sub-video"] : null;
    };
    return Config;
})();

var config = new Config();
//# sourceMappingURL=config.js.map
