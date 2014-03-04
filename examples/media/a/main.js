/// <reference path="../../cdn/jquery.d.ts" />
/// <reference path="../../cdn/a3client/a3.d.ts" />
/// <reference path="config.ts" />
var $start = $("#start");

var MediaListener = (function () {
    function MediaListener() {
    }
    MediaListener.prototype.onMediaReady = function (o) {
        if (!o) {
            this.onMediaFailed();
            return;
        }
        console.log("Media ready", o);
        $start.prop("disabled", false);
        window.setTimeout(function () {
            media.checkHardware();
        }, 10);
    };
    MediaListener.prototype.onMediaMessage = function (type, opt) {
        console.log("MediaMessage", type, opt);
    };
    MediaListener.prototype.onMediaFailed = function () {
        console.log("Media failed");
    };
    return MediaListener;
})();

var media;
function init() {
    var flashVars = config.getFlashVars();
    flashVars["checkMicVolume"] = false;
    console.log("Flash Vars:", flashVars);
    $("#media").html('');
    media = new a3.FlashMedia(new MediaListener(), document.getElementById("media"), flashVars);
    media.start();
}

$start.click(function () {
    var sdpOffer = {
        publishUrlVoice: config.getPubAudio(),
        playUrlVoice: config.getSubAudio(),
        publishUrlVideo: config.getPubVideo(),
        playUrlVideo: config.getSubVideo()
    };
    console.log("sdpOffer:", sdpOffer);
    try  {
        media.setOfferSdp(null, null, sdpOffer);
    } catch (e) {
        debugger;
    }
});

$(".flash-vars-config-toggle").click(function () {
    $("#flash-vars-config").toggleClass("hidden");
    config.apply();
});
$("#flash-vars-apply").click(function () {
    $("#flash-vars-config").addClass("hidden");
    config.apply();
    init();
});

init();
//# sourceMappingURL=main.js.map
