/// <reference path="../jquery.d.ts" />

if (typeof LOG === "undefined")
    LOG = function () {
        var args = [];
        for (var _i = 0; _i < (arguments.length - 0); _i++) {
            args[_i] = arguments[_i + 0];
        }
    };

var BaseConfig = (function () {
    function BaseConfig(_localStorageKey) {
        this._localStorageKey = _localStorageKey;
        this._data = {};
        this._load();
        this._applyUi();
        this._addListeners();
    }
    BaseConfig.prototype.defaults = function () {
        //
        //  ooverride this method to return some default values
        //
        return {};
    };

    BaseConfig.prototype.value = function (key) {
        return this._data[key];
    };
    BaseConfig.prototype.each = function (cb) {
        for (var key in this._data) {
            if (this._data.hasOwnProperty(key)) {
                cb(key, this._data[key]);
            }
        }
    };

    BaseConfig.prototype.apply = function () {
        var _this = this;
        // is called when something is changed on the page
        // - collect data from elements and save
        $("[data-config]").each(function (_, el) {
            var key = $(el).data("config"), $el = $(el);
            var value;
            if (el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
                value = $el.prop("checked");
            } else if (["input", "textarea", "select"].indexOf(el.tagName.toLowerCase()) !== -1) {
                value = $el.val();
                if (value && $el.attr("type") === "number")
                    value = parseInt(value);
            } else {
                value = $el.get(0).className;
            }
            _this._data[key] = value;
        });
        this._save();
    };

    //
    // private
    //
    BaseConfig.prototype._load = function () {
        if (!window.localStorage)
            return;
        var storage = window.localStorage;
        this._data = $.extend(this.defaults(), JSON.parse(storage.getItem(this._localStorageKey)));
    };

    BaseConfig.prototype._save = function () {
        if (!window.localStorage)
            throw new Error("No window.localStorage");
        var storage = window.localStorage;
        storage.setItem(this._localStorageKey, JSON.stringify(this._data));
    };

    BaseConfig.prototype._addListeners = function () {
        var _this = this;
        $("input[data-config], textarea[data-config], select[data-config]").on("change keypress", function () {
            LOG("Config changed");
            _this.apply();
        });
    };

    BaseConfig.prototype._applyUi = function () {
        var _this = this;
        $("[data-config]").each(function (_, el) {
            var key = $(el).data("config"), $el = $(el);
            if (!(key in _this._data)) {
                LOG("Config: Error in data-config: key=" + key);
                return;
            }
            var value = _this._data[key];
            if (el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
                $el.prop("checked", value);
            } else if (["input", "textarea", "select"].indexOf(el.tagName.toLowerCase()) !== -1) {
                $el.val(value);
            } else {
                $el.get(0).className = value;
            }
        });
    };
    return BaseConfig;
})();
//# sourceMappingURL=config.js.map
