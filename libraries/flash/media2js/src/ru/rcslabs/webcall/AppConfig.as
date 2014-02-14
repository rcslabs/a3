package ru.rcslabs.webcall
{
	import ru.rcslabs.config.Config;
	
	public class AppConfig extends Config
	{
		public function AppConfig()
		{
			super();
			
			// call timer for freecall
			defaultValues.callTimerX = 0;
			defaultValues.callTimerY = 0;
			defaultValues.callTimerFontColor = 0;
			defaultValues.callTimerFontSize = 10;
			// click tag for freecall
			defaultValues.clickTag = null;
		}
		
		public function get callTimerX():int{return getPropertyAsNumber('callTimerX');}
		public function get callTimerY():int{return getPropertyAsNumber('callTimerY');}
		public function get callTimerFontColor():int{return getPropertyAsNumber('callTimerFontColor');}
		public function get callTimerFontSize():int{return getPropertyAsNumber('callTimerFontSize');}
		
		public function get clickTag():String{return getPropertyAsString('clickTag');}
	}
}