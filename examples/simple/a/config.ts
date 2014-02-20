/// <reference path="../../cdn/jquery.d.ts" />
/// <reference path="../../cdn/config/config.ts" />

//
// Simple: config
//

class Config extends BaseConfig {

	constructor() {
		super("config-simple");
	}

	public defaults(): any {
		return {
			"sio-signaling": true,
			"webrtc-media": true,
			"service": "click2call"
		};
	}

	//
	// getters
	//
	isSioSignaling()          { return !!this.value("sio-signaling");                        }
	isFlashSignaling()        { return !this.isSioSignaling();                               }
	isWebrtcMedia()           { return !!this.value("webrtc-media");                         }
	isFlashMedia()            { return !this.isWebrtcMedia();                                }
	getSioSignalingEndpoint() { return this.value("sio-signaling-endpoint");                 }
	getUsername()             { return this.value("username");                               }
	getPassword()             { return this.value("password");                               }
	getService()              { return this.value("service");                                }
	getBUri()                 { return this.value("b-uri");                                  }
	getVV()                   { return [!!this.value("vv-voice"), !!this.value("vv-video")]; }
	isInitAutomatically()     { return !!this.value("init-automatically");                   }
}


var config: Config = new Config();
