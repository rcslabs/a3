package com.rcslabs.rcl;

/**
 * Parameters for this RCL API implementation.
 * 
 * These are the following:
 * <ul>
 * 	<li>sipServerAddress - the address of the SIP server</li>
 *  <li>sipServerPort - the SIP server port</li>
 *  <li>sipProxyAddress - the address of the SIP proxy</li>
 *  <li>sipProxyPort - the SIP proxy port</li>
 * 	<li>localIpAddress - the local IP address (default: automatically detected)</li>
 * 	<li>localPort - local SIP port (default: 5062)</li>
 * 	<li>maxForwards - SIP 'MaxForwards' value (default: 70)</li>
 * 	<li>expires - SIP 'Expires' value i.e. the re-register period in seconds (default: 3600)</li>
 * 	<li>automaticDispose - if true, automatically stops SIP stack and releases all resources 
 * 	after all connections are closed (default: true)</li>
 * 	<li>fullLogging - enable ALL underlying SIP stack logs</li>
 * 	<li>presenceEnabled - enable SIP Presence feature</li>
 * 	<li>sipUserAgent - SIP User-Agent header value</li>
 *  <li>enableSipAudit - enable SIP dialog audit to avoid memory leaks</li>
 *  <li>mediaIpAddress - IP address of media server that is put into SDP</li>
 *  <li>conferencePhoneNumber - phone number for establishing conferences</li>
 *  <li>audioCallMaxDuration - maximum audio call duration in seconds, after which 
 *  the call will be droped</li>
 *  <li>videoCallMaxDuration - maximum video call duration in seconds, after which 
 *  the call will be droped</li>
 *  <li>callDurationNotificationTime - time in seconds before call is dropped by 
 *  max duration, in which the notification about this drop will be fired</li>
 *  <li>remoteCallIdentificationHeader - if set, uses custom SIP header to transfer information
 *  about the call ID, thus allowing to detect direct Flash-to-Flash calls</li>
 *  <li>enableExclusiveRegister - if set to true, then prohibit register attempts after denial (default: false)</li>
 *  <li>h264PayloadType - payload type number used for H264 codec (default: 96)</li>
 *  <li>registerBeforeExpirationTime - time in seconds to register next time before previous register expires</li>
 *  <li>subscribeBeforeExpirationTime - time in seconds to subscribe next time before previous subscription expires (default: 10)</li>
 *  <li>publishBeforeExpirationTime - time in seconds to publish next time before previous publish expires (default: 10)</li>
 *  <li>subscribeExpires = SIP 'Expires' value for SUBSCRIBE request in seconds (default: 3600)</li>
 *  <li>publishExpires = SIP 'Expires' value for PUBLISH request in seconds (default: 3600)</li>
 * </ul>
 * 
 * Use getters and setters to configure these values.
 */
public class JainSipGlobalParams implements Cloneable {
	private String sipServerAddress;
	private int sipServerPort;
	private String localIpAddress;
	private int localPort = 5062;
	private int maxForwards = 70;
	private int expires = 3600;
	private boolean automaticDispose = false;
	private boolean fullLogging = false;
	private String sipProxyAddress;
	private int sipProxyPort;
	private String sipUserAgent;
	private boolean enableSipAudit = true;
	private String mediaIpAddress;
	private String conferencePhoneNumber;
	private long audioCallMaxDuration;
	private long videoCallMaxDuration;
	private long callDurationNotificationTime;
	private String remoteCallIdentificationHeader;
	private boolean enableExclusiveRegister = false;
	private Integer h264PayloadType = null;
	private int registerBeforeExpirationTime = 10;
	private int subscribeBeforeExpirationTime = 10;
	private int publishBeforeExpirationTime = 10;
	private int subscribeExpires = 3600;
	private int publishExpires = 3600;
	private boolean use0port4acceptVideoAsAudio = false;
	
	@Override
	protected Object clone() {
		try {
			return super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getSipServerAddress() {
		return sipServerAddress;
	}
	
	public void setSipServerAddress(String sipServerIpAddress) {
		this.sipServerAddress = sipServerIpAddress;
	}
	
	public String getLocalIpAddress() {
		return localIpAddress;
	}
	
	public void setLocalIpAddress(String localIpAddress) {
		this.localIpAddress = localIpAddress;
	}
	
	public int getLocalPort() {
		return localPort;
	}
	
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	
	public int getMaxForwards() {
		return maxForwards;
	}
	
	public void setMaxForwards(int maxForwards) {
		this.maxForwards = maxForwards;
	}

	public void setExpires(int expires) {
		this.expires = expires;
	}

	public int getExpires() {
		return expires;
	}

	public void setAutomaticDispose(boolean automaticDispose) {
		this.automaticDispose = automaticDispose;
	}

	public boolean isAutomaticDispose() {
		return automaticDispose;
	}

	public void setFullLogging(boolean fullLogging) {
		this.fullLogging = fullLogging;
	}

	public boolean isFullLogging() {
		return fullLogging;
	}

	public void setSipProxyAddress(String sipProxyAddress) {
		this.sipProxyAddress = sipProxyAddress;
	}

	public String getSipProxyAddress() {
		return sipProxyAddress;
	}

	public void setSipUserAgent(String sipUserAgent) {
		this.sipUserAgent = sipUserAgent;
	}

	public String getSipUserAgent() {
		return sipUserAgent;
	}

	public void setEnableSipAudit(boolean enableSipAudit) {
		this.enableSipAudit = enableSipAudit;
	}

	public boolean isEnableSipAudit() {
		return enableSipAudit;
	}

	public String getMediaIpAddress() {
		return mediaIpAddress;
	}

	public void setMediaIpAddress(String mediaIpAddress) {
		this.mediaIpAddress = mediaIpAddress;
	}

	public int getSipServerPort() {
		return sipServerPort;
	}

	public void setSipServerPort(int sipServerPort) {
		this.sipServerPort = sipServerPort;
	}

	public int getSipProxyPort() {
		return sipProxyPort;
	}

	public void setSipProxyPort(int sipProxyPort) {
		this.sipProxyPort = sipProxyPort;
	}

	public String getConferencePhoneNumber() {
		return conferencePhoneNumber;
	}

	public void setConferencePhoneNumber(String conferencePhoneNumber) {
		this.conferencePhoneNumber = conferencePhoneNumber;
	}

	public long getAudioCallMaxDuration() {
		return audioCallMaxDuration;
	}

	public void setAudioCallMaxDuration(long audioCallMaxDuration) {
		this.audioCallMaxDuration = audioCallMaxDuration;
	}

	public long getVideoCallMaxDuration() {
		return videoCallMaxDuration;
	}

	public void setVideoCallMaxDuration(long videoCallMaxDuration) {
		this.videoCallMaxDuration = videoCallMaxDuration;
	}

	public long getCallDurationNotificationTime() {
		return callDurationNotificationTime;
	}

	public void setCallDurationNotificationTime(long callDurationNotificationTime) {
		this.callDurationNotificationTime = callDurationNotificationTime;
	}

	public String getRemoteCallIdentificationHeader() {
		return remoteCallIdentificationHeader;
	}

	public void setRemoteCallIdentificationHeader(String remoteCallIdentificationHeader) {
		this.remoteCallIdentificationHeader = remoteCallIdentificationHeader;
	}

	@Override
	public String toString() {
		return String
				.format("JainSipGlobalParams [sipServerAddress=%s, sipServerPort=%s, localIpAddress=%s, localPort=%s, maxForwards=%s, expires=%s, automaticDispose=%s, fullLogging=%s, sipProxyAddress=%s, sipProxyPort=%s, sipUserAgent=%s, enableSipAudit=%s, mediaIpAddress=%s, conferencePhoneNumber=%s, audioCallMaxDuration=%s, videoCallMaxDuration=%s, callDurationNotificationTime=%s]",
						sipServerAddress, sipServerPort, localIpAddress, localPort, maxForwards, expires,
						automaticDispose, fullLogging, sipProxyAddress, sipProxyPort, sipUserAgent,
						enableSipAudit, mediaIpAddress, conferencePhoneNumber, audioCallMaxDuration,
						videoCallMaxDuration, callDurationNotificationTime);
	}

	public boolean isEnableExclusiveRegister() {
		return enableExclusiveRegister;
	}

	public void setEnableExclusiveRegister(boolean enableExclusiveRegister) {
		this.enableExclusiveRegister = enableExclusiveRegister;
	}

	public Integer getH264PayloadType() {
		return h264PayloadType;
	}

	public void setH264PayloadType(Integer h264PayloadType) {
		this.h264PayloadType = h264PayloadType;
	}

	public int getRegisterBeforeExpirationTime() {
		return registerBeforeExpirationTime;
	}

	public void setRegisterBeforeExpirationTime(int registerBeforeExpirationTime) {
		this.registerBeforeExpirationTime = registerBeforeExpirationTime;
	}

	public int getSubscribeBeforeExpirationTime() {
		return subscribeBeforeExpirationTime;
	}

	public void setSubscribeBeforeExpirationTime(int subscribeBeforeExpirationTime) {
		this.subscribeBeforeExpirationTime = subscribeBeforeExpirationTime;
	}

	public int getPublishBeforeExpirationTime() {
		return publishBeforeExpirationTime;
	}

	public void setPublishBeforeExpirationTime(int publishBeforeExpirationTime) {
		this.publishBeforeExpirationTime = publishBeforeExpirationTime;
	}

	public int getSubscribeExpires() {
		return subscribeExpires;
	}

	public void setSubscribeExpires(int subscribeExpires) {
		this.subscribeExpires = subscribeExpires;
	}

	public int getPublishExpires() {
		return publishExpires;
	}

	public void setPublishExpires(int publishExpires) {
		this.publishExpires = publishExpires;
	}

	public boolean isUse0port4acceptVideoAsAudio() {
		return use0port4acceptVideoAsAudio;
	}

	public void setUse0port4acceptVideoAsAudio(boolean use0port4acceptVideoAsAudio) {
		this.use0port4acceptVideoAsAudio = use0port4acceptVideoAsAudio;
	}


}
