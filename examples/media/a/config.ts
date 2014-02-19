/// <reference path="jquery.d.ts" />


var $config: JQuery = $(".config");

class Config {
  private _data: any = {
    "pub-audio": "rtmp://192.168.1.200/live/subaudio",
		"sub-audio": "rtmp://192.168.1.200/live/pubaudio",
		"sub-video": "rtmp://192.168.1.200/live/pubvideo",
		"pub-video": "rtmp://192.168.1.200/live/subvideo",
		"is-pub-audio": true,
		"is-sub-audio": true,
		"is-pub-video": false,
		"is-sub-video": false
  };

  constructor() {
    this.load();
    this.applyUi();
    this.addListeners();
  }

  load() {
    if(!window.localStorage) return;
    var storage: Storage = window.localStorage;
    $.extend(this._data, JSON.parse(storage.getItem("config-media")));
  }
  save() {
    if(!window.localStorage) throw new Error("No window.localStorage");
    var storage: Storage = window.localStorage;
    storage.setItem("config-media", JSON.stringify(this._data));
  }
  addListeners() {
    $("input[data-config], textarea[data-config]").on("change keypress", () => {
      console.log("Config changed");
      this.applyData();
      this.save();
    });
  }

  applyUi() {
    $("[data-config]").each((_, el) => {
      var key: string = $(el).data("config"), $el: JQuery = $(el);
      if(!(key in this._data)) {
        console.warn("Config: Error in data-config: key=" + key);
        return;
      }
      var value: any = this._data[key];
      if(el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
        $el.prop("checked", value);
      } else if(el.tagName.toLowerCase() === "input" || el.tagName.toLowerCase() === "textarea") {
        $el.val(value);
      } else {
        $el.get(0).className = value;
      }
    });
  }
  applyData() {
    $("[data-config]").each((_, el) => {
      var key: string = $(el).data("config"), $el: JQuery = $(el);
      var value: any;
      if(el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
        value = $el.prop("checked");
      } else if(el.tagName.toLowerCase() === "input" || el.tagName.toLowerCase() === "textarea") {
        value = $el.val();
      } else {
        value = $el.get(0).className;
      }
      this._data[key] = value;
    });
  }

	getPubAudio() { return this._data["is-pub-audio"] ? this._data["pub-audio"] : null }
	getSubAudio() { return this._data["is-sub-audio"] ? this._data["sub-audio"] : null }
	getPubVideo() { return this._data["is-pub-video"] ? this._data["pub-video"] : null }
	getSubVideo() { return this._data["is-sub-video"] ? this._data["sub-video"] : null }
}


var config: Config = new Config();

