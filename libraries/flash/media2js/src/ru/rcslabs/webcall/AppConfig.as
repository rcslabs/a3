package ru.rcslabs.webcall
{
	import ru.rcslabs.config.Config;
	import flash.media.SoundCodec;

	public class AppConfig extends Config
	{
		function AppConfig(){
			super();
			defaultValues.micCodec = SoundCodec.SPEEX;
		}
	}
}