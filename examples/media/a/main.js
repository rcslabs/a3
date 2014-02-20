/// <reference path="jquery.d.ts" />
/// <reference path="media.ts" />
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
function applyFlashVars() {
    console.log("Flash Vars:", config.getFlashVars());
    $("#media").html('<div id="media-container" class="media-container"></div>');
    media = new a3.FlashMedia(new MediaListener(), document.getElementById("media-container"), config.getFlashVars());
    media.start();
}

applyFlashVars();

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
    config.applyData();
    config.save();
});
$("#flash-vars-apply").click(function () {
    $("#flash-vars-config").addClass("hidden");
    config.applyData();
    config.save();
    applyFlashVars();
});
//# sourceMappingURL=main.js.map
