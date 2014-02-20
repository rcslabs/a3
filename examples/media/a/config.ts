/// <reference path="../../cdn/jquery.d.ts" />
/// <reference path="../../cdn/config/config.ts" />

//
// MediaTester config
//

class Config extends BaseConfig {

  constructor() {
		super("config-media-tester");
  }

	public defaults(): any {
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
	}

	//
	// getters
	//
	public getPubAudio() { return this.value("is-pub-audio") ? this.value("pub-audio") : null }
	public getSubAudio() { return this.value("is-sub-audio") ? this.value("sub-audio") : null }
	public getPubVideo() { return this.value("is-pub-video") ? this.value("pub-video") : null }
	public getSubVideo() { return this.value("is-sub-video") ? this.value("sub-video") : null }

	public getFlashVars() {
		var result = {};
		this.each((key, value) => {
			if(key.indexOf("fv-") === 0 && value) {
				result[key.substr(3)] = value;
			}
		});
		return result;
	}
}

var config: Config = new Config();
