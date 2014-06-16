/// <reference path="../../cdn/jquery.d.ts" />
/// <reference path="../../cdn/a3client/a3.d.ts" />
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
			media.checkHardware([true, true]);
		},10);
	}
	onMediaMessage(type: string, opt: any) {
		console.log("MediaMessage", type, opt);
	}
	onMediaFailed() {
		console.log("Media failed");
	}
}


var media: a3.FlashMedia;
function init() {
	var flashVars = config.getFlashVars();
	flashVars["checkMicVolume"] = false;
	console.log("Flash Vars:", flashVars );
	$("#media").html('');
	media = new a3.FlashMedia(new MediaListener(), document.getElementById("media"), flashVars);
	media.start();
}


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

$(".flash-vars-config-toggle").click(() => { $("#flash-vars-config").toggleClass("hidden"); config.apply();	});
$("#flash-vars-apply").click(() => { $("#flash-vars-config").addClass("hidden");  config.apply();	init(); });


init();
