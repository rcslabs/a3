/*@@easydoc-start, id=rtmp2js@@
<h2>MEDIA2JS</h2>
<a href="https://doc.rcslabs.ru/index.php/%D0%9A%D0%BE%D0%BC%D0%BF%D0%BE%D0%BD%D0%B5%D0%BD%D1%82%D1%8B_SWF2JS">See doc here</a>
@@easydoc-end@@
*/

package
{
	import flash.display.Sprite;
	import flash.external.ExternalInterface;
	import flash.net.registerClassAlias;
	import flash.system.Capabilities;
	import flash.system.Security;
	
	import mx.logging.Log;
	import mx.logging.LogEventLevel;
	import mx.logging.LogLogger;
	import mx.logging.targets.TraceTarget;
	
	import ru.rcslabs.net.IConnector;
	import ru.rcslabs.net.IConnectorListener;
	import ru.rcslabs.net.connection.RTMPConnector;
	import ru.rcslabs.storage.IDataStorage;
	import ru.rcslabs.storage.SharedDataStorage;
	import ru.rcslabs.storage.UserDataStorage;
	import ru.rcslabs.utils.Logger;
	import ru.rcslabs.webcall.api.ICallClient;
	import ru.rcslabs.webcall.api.events.CallEvent;
	import ru.rcslabs.webcall.api.events.SessionEvent;
	import ru.rcslabs.webcall.business.calls.CallService;
	import ru.rcslabs.webcall.vo.CaptchaParamsVO;
	import ru.rcslabs.webcall.vo.ClientInfoVO;
	
	[SWF(width="216", height="180", frameRate="30")]
	public class RTMP2JS extends Sprite  implements IConnectorListener, ICallClient
	{	
		private var callService:CallService;
		
		private var conn:RTMPConnector;
		
		private var sharedStorage:IDataStorage;
		
		private var userStorage:IDataStorage;
		
		private var credentials:Object;
		
		private var info:ClientInfoVO;
		
		private var data:Object;
		
		private static const JSHANDLER:String = "webcallMessageHandler";
		
		public function RTMP2JS()
		{
			super();
			Security.allowDomain("*");
			Security.allowInsecureDomain("*");
			if(ExternalInterface.available) 
				runInBrowser();
			else
				runAsDebug();
		}	
		
		private function runInBrowser():void
		{
			var p:Object = loaderInfo.parameters;		
			if(undefined == p['endpoint']){throw new Error("Endpoint not defined");}
			Logger.init(undefined != p['logLevel'] ? p['logLevel'] : "NONE");
			
			info = ClientInfoVO.createInfo();
			
			conn = new RTMPConnector();
			conn.addEndpoint( p['endpoint'] );
			conn.reuseSuccessOnly = true;
			conn.addConnectionListener(this);
			
			callService = new CallService();
			callService.serviceName = "callService";
			callService.connector = conn;
			
			sharedStorage = new SharedDataStorage();
			
			conn.addMessageHandler('onCallEvent', onCallEvent);
			conn.addMessageHandler('onConnectionEvent', onSessionEvent);
			conn.addMessageHandler('onVerificationFailed', onVerificationFailed);
			
			ExternalInterface.addCallback("getVersion", getVersion);				
			ExternalInterface.addCallback("webcallRequest", webcallRequest);				
			ExternalInterface.addCallback("storage", storage);
			ExternalInterface.addCallback("setClientInfo", setClientInfo);
			ExternalInterface.call("swfReady", "ru.rcslabs.webcall.SignalTransport");
						
			if(Log.isDebug()){
				Log.getLogger(Logger.DEFAULT_CATEGORY).debug(loaderInfo.url);
				Log.getLogger(Logger.DEFAULT_CATEGORY).debug(info.pageUrl);				
				Log.getLogger(Logger.DEFAULT_CATEGORY).debug(info.userAgent);
			}			
		}
		
		private function runAsDebug():void
		{			
			conn = new RTMPConnector();
			conn.addEndpoint("rtmp://192.168.1.230/webcall2/communicator");
			conn.reuseSuccessOnly = true;
			conn.addConnectionListener(this);
			
			callService = new CallService();
			callService.serviceName = "callService";
			callService.connector = conn;
			
			sharedStorage = new SharedDataStorage();
			
			conn.addMessageHandler('onCallEvent', onCallEvent);
			conn.addMessageHandler('onConnectionEvent', onSessionEvent);
			conn.addMessageHandler('onVerificationFailed', onVerificationFailed);
			
			info = new ClientInfoVO();
			webcallRequest("open", {phone : "1010", password : "1234"});		
		}
		
		// JS->AS
		
		private function setClientInfo(prop:String, value:String):void
		{
			if('pageUrl' == prop){
				info.pageUrl = value;
				Log.getLogger(Logger.DEFAULT_CATEGORY).debug(info.pageUrl);
			}else{
				throw new Error("Invalid param {"+prop+"}");
			}			
		}
		
		private function getVersion():String
		{
			var s:String = WEBCALL::APP_VERSION;
			if(CallService.SERVICE_VERSION){
				s = s.concat(", ", CallService.SERVICE_VERSION[0], ".", CallService.SERVICE_VERSION[1]);
			}
			return s;
		}
		
		private function webcallRequest(name:String, params:Object=null):void
		{
			switch(name)
			{
				case("open"): 
					credentials = {
						username : params.phone, 
						password : params.password,
						captcha : null
					}; 
					
					if(undefined != params.challenge && undefined != params.code){
						var p:CaptchaParamsVO = new CaptchaParamsVO();
						p.challenge = params.challenge;
						p.response = params.code;
						credentials.captcha = p;
					}	
					
					data = {};
					
					for(var k:String in params){
						if(-1 == ["phone", "password", "challenge", "code"].indexOf(k)){
							Log.getLogger(Logger.DEFAULT_CATEGORY).info("Additional parameter " + k + "=" + params[k]);
							data[k] = params[k];
						}
					}
					
					conn.connect();
					break;
				
				case("close"): 		
					callService.close(); 
					break;
				
				case("accept"): 	
					callService.acceptCall(params.callId, params.av_params); 
					break;
				
				case("decline"): 	
					callService.declineCall(params.callId); 
					break;
				
				case("hangup"): 	
					if(callService.getCallById(params.callId)) 
						callService.hangupCall(params.callId); 
					break;
				
				case("call"): 		
					callService.startCall(params.destination, params.av_params); 
					break;
				
				case("dtmf"): 		
					callService.sendDTMF(params.callId, params.dtmf); 
					break;
				
				default: 
					throw new Error("Unknown request name {"+name+"}");
			}
		}
		
		private function storage(target:String, callName:String, key:String=null, value:*=null):*
		{
			var s:IDataStorage;
			if('user' == target){
				s = userStorage;
			}else if('shared' == target){
				s = sharedStorage;
			}	
			
			if(null == s){return;}
			var i:int = ['hasKey', 'getValue', 'setValue', 'deleteValue', 'clear'].indexOf(callName);
			if(-1 == i){return;}	
			
			switch(i){
				case(0): return s.hasKey(key);
				case(1): return s.getValue(key);
				case(2): return s.setValue(key, value);
				case(3): return s.deleteValue(key);
				case(4): return s.clear();
			}
		}
		
		/** IConnectorListener */
		
		public function onConnectionConnect(connector:IConnector):void
		{
			if(ExternalInterface.available)
				ExternalInterface.call(JSHANDLER, new SessionEvent(SessionEvent.CONNECTING));
		}
		
		public function onConnectionConnected(connector:IConnector):void
		{
			userStorage = new UserDataStorage(credentials.username);
			if(!credentials.captcha){
				callService.open(credentials.username, credentials.password, info, data);
			} else {
				callService.open(credentials.username, credentials.password, info, data, credentials.captcha);
			}
			
			callService.getServiceVersion();
		}
		
		public function onConnectionFailed(connector:IConnector):void
		{
			if(ExternalInterface.available)			
				ExternalInterface.call(JSHANDLER, new SessionEvent(SessionEvent.CONNECTION_FAILED));
		}
		
		public function onConnectionClosed(connector:IConnector):void
		{
			if(ExternalInterface.available)			
				ExternalInterface.call(JSHANDLER, new SessionEvent(SessionEvent.CONNECTION_BROKEN));
		}		
		
		/** ICallClient */
		
		public function onCallEvent(event:CallEvent):void
		{
			if(ExternalInterface.available)			
				ExternalInterface.call(JSHANDLER, event);
		}
		
		public function onSessionEvent(event:SessionEvent):void
		{
			if(SessionEvent.CONNECTED == event.type){
				callService.connector.connectionUid = event.connectionUid;
			}
			
			if(ExternalInterface.available)			
				ExternalInterface.call(JSHANDLER, event);			
		}
		
		public function onVerificationFailed(uid:String, reason:String, message:String):void
		{
			var event:SessionEvent = new SessionEvent("VERIFICATION_FAILED");
			event.connectionUid = conn.connectionUid;
			event.message = message;
			
			if(ExternalInterface.available)
				ExternalInterface.call(JSHANDLER, event);
		}
		
	}
}