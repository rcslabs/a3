/// <reference path="jquery.d.ts" />


var $config: JQuery = $(".config");

class Config {
  private _data: any = {
    "sio-signaling": true,
    "webrtc-media": true
  };

  constructor() {
    this.load();
    this.applyUi();
    this.addListeners();
  }

  load() {
    if(!window.localStorage) return;
    var storage: Storage = window.localStorage;
    $.extend(this._data, JSON.parse(storage.getItem("config-data")));
  }
  save() {
    if(!window.localStorage) throw new Error("No window.localStorage");
    var storage: Storage = window.localStorage;
    storage.setItem("config-data", JSON.stringify(this._data));
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
      if(el.tagName.toLowerCase() === "input" && $el.attr("type") === "radio") {
        $el.prop("checked", value);
      } else {
        $el.val(value);
      }
    });
  }
  applyData() {
    $("[data-config]").each((_, el) => {
      var key: string = $(el).data("config"), $el: JQuery = $(el);
      var value: any;
      if(el.tagName.toLowerCase() === "input" && $el.attr("type") === "radio") {
        value = $el.prop("checked");
      } else {
        value = $el.val();
      }
      this._data[key] = value;
    });
  }

  isSioSignaling()          { return !!this._data["sio-signaling"];       }
  isFlashSignaling()        { return !this.isSioSignaling();              }
  isWebrtcMedia()           { return !!this._data["webrtc-media"];        }
  isFlashMedia()            { return !this.isWebrtcMedia();               }
  getSioSignalingEndpoint() { return this._data["sio-signaling-endpoint"];}
  getUsername()             { return this._data["username"];              }
  getPassword()             { return this._data["password"];              }
  getService()              { return this._data["service"];               }
}


var config: Config = new Config();
