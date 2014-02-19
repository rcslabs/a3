/// <reference path="jquery.d.ts" />
/// <reference path="media.ts" />
/// <reference path="config.ts" />

var $start = $("#start");

class MediaListener implements a3.IMediaListener {
	onMediaReady(o: a3.IMedia) {
		if(!o) {
			this.onMediaFailed();
			return;
		}
		console.log("Media ready", o);
		$start.prop("disabled", false);
		window.setTimeout(() => {
			media.checkHardware();
		},10);
	}
	onMediaMessage(type: string, opt: any) {
		console.log("MediaMessage", type, opt);
	}
	onMediaFailed() {
		console.log("Media failed");
	}
}

var flashVars = {};
var media = new a3.FlashMedia(new MediaListener(), document.getElementById("media-container"), flashVars);
media.start();

$start.click(() => {

	var sdpOffer = {
		publishUrlVoice: 	config.getPubAudio(),
		playUrlVoice: 		config.getSubAudio(),
		publishUrlVideo: 	config.getPubVideo(),
		playUrlVideo:     config.getSubVideo()
	};
	console.log("sdpOffer:", sdpOffer);
	try {
		media.setOfferSdp(null, null, sdpOffer);
	}catch(e) {
		debugger;
	}
});
