/// <reference path="../jquery.d.ts" />

declare var LOG;

if(typeof LOG === "undefined") LOG = (...args) => {};

class BaseConfig {

	private _data: any = {};

	constructor(private _localStorageKey: string) {
		this._load();
		this._applyUi();
		this._addListeners();
	}

	public defaults(): any {
		//
		//  ooverride this method to return some default values
		//
		return {};
	}

	public value(key: string): any {
		return this._data[key];
	}
	public each(cb: (key: string, value: any) => void) {
		for(var key in this._data) {
			if(this._data.hasOwnProperty(key)){
				cb(key, this._data[key]);
			}
		}
	}

	public apply() {
		// is called when something is changed on the page
		// - collect data from elements and save
		$("[data-config]").each((_, el) => {
			var key: string = $(el).data("config"), $el: JQuery = $(el);
			var value: any;
			if(el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
				value = $el.prop("checked");
			} else if(["input", "textarea", "select"].indexOf(el.tagName.toLowerCase()) !== -1) {
				value = $el.val();
				if(value && $el.attr("type") === "number") value = parseInt(value);
			} else {
				value = $el.get(0).className;
			}
			this._data[key] = value;
		});
		this._save();
	}

	//
	// private
	//
	private _load() {
		if(!window.localStorage) return;
		var storage: Storage = window.localStorage;
		this._data = $.extend(this.defaults(), JSON.parse(storage.getItem(this._localStorageKey)));
	}

	private _save() {
		if(!window.localStorage) throw new Error("No window.localStorage");
		var storage: Storage = window.localStorage;
		storage.setItem(this._localStorageKey, JSON.stringify(this._data));
	}

	private _addListeners() {
		$("input[data-config], textarea[data-config], select[data-config]").on("change keypress", () => {
			LOG("Config changed");
			this.apply();
		});
	}

	private _applyUi() {
		$("[data-config]").each((_, el) => {
			var key: string = $(el).data("config"), $el: JQuery = $(el);
			if(!(key in this._data)) {
				LOG("Config: Error in data-config: key=" + key);
				return;
			}
			var value: any = this._data[key];
			if(el.tagName.toLowerCase() === "input" && ($el.attr("type") === "radio" || $el.attr("type") === "checkbox")) {
				$el.prop("checked", value);
			} else if(["input", "textarea", "select"].indexOf(el.tagName.toLowerCase()) !== -1) {
				$el.val(value);
			} else {
				$el.get(0).className = value;
			}
		});
	}
}
