/*@@easydoc-start, id=media2js@@
<h2>MEDIA2JS</h2>
<a href="https://doc.rcslabs.ru/index.php/%D0%9A%D0%BE%D0%BC%D0%BF%D0%BE%D0%BD%D0%B5%D0%BD%D1%82%D1%8B_SWF2JS">See doc here</a>
@@easydoc-end@@
*/

package
{
	import flash.display.Sprite;
	import flash.display.StageAlign;
	import flash.display.StageScaleMode;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.external.ExternalInterface;
	import flash.geom.Rectangle;
	import flash.media.Sound;
	import flash.media.SoundChannel;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.system.Capabilities;
	import flash.system.Security;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.utils.Timer;
	import flash.utils.setTimeout;
	
	import mx.logging.Log;
	
	import ru.rcslabs.components.ImageLoader;
	import ru.rcslabs.components.VideoContainer;
	import ru.rcslabs.components.settings.SettingsPanel;
	import ru.rcslabs.config.Config;
	import ru.rcslabs.utils.DragHelper;
	import ru.rcslabs.utils.Logger;
	import ru.rcslabs.webcall.*;
	import ru.rcslabs.webcall.events.MediaTransportEvent;
	import ru.rcslabs.webcall.vo.ClientInfoVO;
	
	[SWF(width="216", height="180", frameRate="30")]
	public class MEDIA2JS extends Sprite
	{
		public var subCont:VideoContainer;
		
		public var pubCont:VideoContainer;
		
		private var config:AppConfig;
		
		private var mt:MediaTransportJS;
		
		private var bg:ImageLoader;
		
		private var dm:DragHelper;
		
		private var tf:TextField;
		
		private var tmr:Timer;
		
		private var dialSoundChannel:SoundChannel;
		
		public function MEDIA2JS()
		{
			super();
			Security.allowDomain("*");
			Security.allowInsecureDomain("*");						
			stage.scaleMode = StageScaleMode.NO_SCALE;
			stage.align = StageAlign.TOP_LEFT;
			
			config = new AppConfig();
			
			if(ExternalInterface.available)
			{
				config.initFromFlashVars(loaderInfo.parameters);				
				
				Logger.init(config.logLevel);

				if(Log.isDebug()){
					var info:ClientInfoVO = ClientInfoVO.createInfo();
					Log.getLogger(Logger.DEFAULT_CATEGORY).debug(loaderInfo.url);
					Log.getLogger(Logger.DEFAULT_CATEGORY).debug(info.pageUrl);				
					Log.getLogger(Logger.DEFAULT_CATEGORY).debug(info.userAgent);
					Log.getLogger(Logger.DEFAULT_CATEGORY).debug("MEDIA2JS " + WEBCALL::APP_VERSION);
					Log.getLogger(Logger.DEFAULT_CATEGORY).debug(config.toString());
				}
				
				mt = new MediaTransportJS();
				mt.setAppContext(this);								
				mt.init(config);
				
				mt.addEventListener(MediaTransportEvent.PUBLISHED, pubHandler);
				mt.addEventListener(MediaTransportEvent.UNPUBLISHED, unpubHandler);
				mt.addEventListener(MediaTransportEvent.SUBSCRIBED, subHandler);
				mt.addEventListener(MediaTransportEvent.UNSUBSCRIBED, unsubHandler);
				
				bg = addChild(new ImageLoader) as ImageLoader;
				
				if(config.clickTag){
					bg.addEventListener(MouseEvent.CLICK, function(e:MouseEvent):void{
						navigateToURL(new URLRequest(config.clickTag), "_blank");	
					});
				}
				
				subCont = addChild(new VideoContainer) as VideoContainer;
				subCont.setSizeAndPositionAsRectangle(config.subscriberRect);
				subCont.visible = false;

				pubCont = addChild(new VideoContainer) as VideoContainer;
				pubCont.setSizeAndPositionAsRectangle(config.publisherRect);
				pubCont.visible = false;

				if(0 != config.callTimerX){
					tmr = new Timer(1000);
					tmr.addEventListener(TimerEvent.TIMER, tmrHandler);					
					tf = createTimerTextField();
					addChild(tf);
				}
				
				dm = new DragHelper();
				dm.allowDrag(pubCont, new Rectangle(
					config.subscriberRect.x, 
					config.subscriberRect.y,
					config.subscriberRect.width - config.publisherRect.width,
					config.subscriberRect.height - config.publisherRect.height
				));
				
				ExternalInterface.marshallExceptions = true;
				
				ExternalInterface.addCallback("muteSubscriber", muteSubscriber);
				ExternalInterface.addCallback("playDialingSound", playDialingSound);
				ExternalInterface.addCallback("stopDialingSound", stopDialingSound);
				ExternalInterface.addCallback("showSettingsPanel", showSettingsPanel);
				ExternalInterface.addCallback("hideSettingsPanel", hideSettingsPanel);
				ExternalInterface.addCallback("loadImage", loadImage);
				ExternalInterface.addCallback("setViewState", setViewState);
				ExternalInterface.call("swfReady", "ru.rcslabs.webcall.MediaTransport");
			}
			
			stage.addEventListener(Event.RESIZE, resizeHandler);
			resizeHandler();
		}
		
		
		private function setViewState(state:String):void
		{
			if('default' == state){ 
				if(tf) tf.text = ''; 
			}else if('timerOn' == state){
				if(tmr) {tmr.reset(); tmr.start();}
			}else if('timerOff' == state){
				if(tmr) { tmr.stop(); }
			}
		}
		
		public function tmrHandler(event:TimerEvent):void
		{
			if(!tf) return;
			var d:Date = new Date();
			d.time = event.target.currentCount * 1000;
			var s:String = String(d.secondsUTC < 10 ? "0"+d.secondsUTC : d.secondsUTC);
			var m:String = String(d.minutesUTC < 10 ? "0"+d.minutesUTC : d.minutesUTC);
			var h:String = String(d.hoursUTC);
			if(d.hoursUTC){
				tf.text = h.concat(":", m, ":", s);
			}else{
				tf.text = "".concat(m, ":", s);
			}
		}
		
		private function createTimerTextField():TextField
		{
			var ttf:TextFormat = new TextFormat();
			ttf.size = config.callTimerFontSize;
			ttf.color = config.callTimerFontColor;

			var tf:TextField = new TextField();
			tf.x = config.callTimerX;
			tf.y = config.callTimerY;
			tf.selectable = false;
			tf.defaultTextFormat = ttf;
			return tf;
		}
		
		private function resizeHandler(event:Event=null):void
		{
//			trace(stage.stageWidth, stage.stageHeight);
			if(subCont){subCont.setSizeAndPositionAsRectangle(new Rectangle(0,0,stage.stageWidth,stage.stageHeight));} 
			if(pubCont){pubCont.x = pubCont.y = 0; } 
		}
		
		private function playDialingSound():void
		{
			if(null != dialSoundChannel){ return; }
			var s:Sound = SoundManager.createSound(SoundManager.SND_DIAL);
			dialSoundChannel = s.play(0, 30);		
		}
		
		private function stopDialingSound():void
		{
			if(null == dialSoundChannel){ return; }
			dialSoundChannel.stop();
			dialSoundChannel = null;
		}
		
		private function showSettingsPanel(centerPanel:Boolean=false, index:int=0):void
		{
			var sp:SettingsPanel = SettingsPanel.show(stage, index);
			
			if(centerPanel){
				setTimeout(function():void{
					sp.x = int((stage.stageWidth-sp.width)/2);
					sp.y = int((stage.stageHeight-sp.height)/2);				
				}, 300);
			}
		}
		
		private function hideSettingsPanel():void{
			SettingsPanel.hide();
		}
		
		private function loadImage(url:String=null):void
		{
			if(null == url){
				bg.unload();
			}else{
				bg.load(url);
			}
		}
		
		private function pubHandler(e:MediaTransportEvent):void
		{
			pubCont.visible = (null != mt.publisherVideo);
			pubCont.video = mt.publisherVideo;	
		}
		
		private function subHandler(e:MediaTransportEvent):void
		{
			subCont.visible = (null != mt.subscriberVideo);
			subCont.video = mt.subscriberVideo;
		}

		private function unpubHandler(e:MediaTransportEvent):void
		{
			pubCont.video = null;
			pubCont.visible = false;
		}

		private function unsubHandler(e:MediaTransportEvent):void
		{
			subCont.video = null;
			subCont.visible = false;
		}

		private function muteSubscriber(value:Boolean):void
		{
			mt.voiceSubscriber.volume = (value ? 0 : 1);	
		}
	}
}