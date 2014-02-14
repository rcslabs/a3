package com.rcslabs.rcl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.entity.CallType;
import com.rcslabs.rcl.telephony.entity.IMediaParams;
import com.rcslabs.rcl.telephony.entity.MediaParams;
import com.rcslabs.rcl.telephony.entity.MediaType;

public class SdpHelper {
	private static final Integer DEFAULT_VIDEO_MEDIA_FORMAT = 96;
	private static Logger log = LoggerFactory.getLogger(SdpHelper.class);
	private static String templateVideo = readResource("/sdp.conf");
	private static String templateAudio = readResource("/sdp-audio.conf");
	private static Pattern h264pattern = Pattern.compile("^a\\=rtpmap\\:(\\d+)\\s+[hH]264(|\\/.*)\\s*$"); 
	private SdpFactory sdpFactory = SdpFactory.getInstance();
	private String ipAddress = "127.0.0.1";
	private int h264PayloadType = DEFAULT_VIDEO_MEDIA_FORMAT;
	private boolean use0port4acceptVideoAsAudio = false;
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public void setH264PayloadType(int payloadType) {
		this.h264PayloadType = payloadType;
	}
	
	public int getH264PayloadType(ICall call, IMediaParams params) {
		Set<Integer> videoMediaFormats = params.getVideoMediaFormats();
		Integer videoMediaFormat = (
				videoMediaFormats.size() == 0 ? 
						h264PayloadType : 
							videoMediaFormats.iterator().next()
				);
		return videoMediaFormat;
		
	}
	
	public String generateSdp(ICall call, IMediaParams params) {
		String phoneNumber = "FIXME: "; //call.getParams().getFromPhoneNumber();
		if (phoneNumber.startsWith("tel:")) {
			phoneNumber = phoneNumber.substring(4);
		} else if (phoneNumber.startsWith("sip:")) {
			phoneNumber = phoneNumber.substring(4);
		}
		if (phoneNumber.contains("@")) {
			phoneNumber = phoneNumber.substring(0, phoneNumber.indexOf("@"));
		}
		
//		Set<Integer> videoMediaFormats = params.getVideoMediaFormats();
//		Integer videoMediaFormat = (
//				videoMediaFormats.size() == 0 ? 
//						h264PayloadType : 
//							videoMediaFormats.iterator().next()
//				);
		Integer videoMediaFormat = getH264PayloadType(call, params);
		
		return (params.getMediaType() == MediaType.AUDIO ? templateAudio : templateVideo)
		.replaceAll("\\{from-phone-number\\}", phoneNumber)
		.replaceAll("\\{local-ip-address\\}", ipAddress)
		.replaceAll("\\{local-audio-port\\}", String.valueOf(params.getLocalAudioPort()))
		.replaceAll("\\{local-video-port\\}", String.valueOf(params.getLocalVideoPort()))
		.replaceAll("\\{video-media-format\\}", String.valueOf(videoMediaFormat));
	}

	public String generateSdpAccept(ICall call, IMediaParams params, CallType initialCallType) {
		String phoneNumber = "FIXME: ";//call.getParams().getFromUri().getPhoneNumber();

		Integer videoMediaFormat = getH264PayloadType(call, params);
		
		return (this.use0port4acceptVideoAsAudio ? 
				(initialCallType==CallType.AUDIO ? templateAudio : templateVideo) :
				(params.getMediaType() == MediaType.AUDIO ? templateAudio : templateVideo))
		.replaceAll("\\{from-phone-number\\}", phoneNumber)
		.replaceAll("\\{local-ip-address\\}", ipAddress)
		.replaceAll("\\{local-audio-port\\}", String.valueOf(params.getLocalAudioPort()))
		.replaceAll("\\{local-video-port\\}", String.valueOf(params.getLocalVideoPort()))
		.replaceAll("\\{video-media-format\\}", String.valueOf(videoMediaFormat));
	}

	@SuppressWarnings("unchecked")
	public MediaParams parseSdp(String rawSdp) throws SdpException {
		MediaParams ret = new MediaParams();
		SessionDescription sd = sdpFactory.createSessionDescription(rawSdp);
		ret.setRemoteAddress(sd.getConnection().getAddress());
		log.debug("remoteAddress = {}", ret.getRemoteAddress());
		Vector<MediaDescription> mediaDescriptions = sd.getMediaDescriptions(false);
		for(MediaDescription md : mediaDescriptions) {
			if("audio".equals(md.getMedia().getMediaType())) {
				ret.setRemoteAudioPort(md.getMedia().getMediaPort());
				log.debug("remoteAudioPort = {}", ret.getRemoteAudioPort());
				Vector<String> mediaFormats = md.getMedia().getMediaFormats(false);
				log.debug("mediaFormats = {}", mediaFormats);
				ret.setAudioMediaFormats(mediaFormats);
				String ptime = md.getAttribute("ptime");
				if(ptime != null) {
					log.debug("ptime = {}", ptime);
					ret.setPtime(new Integer(ptime));
				}
			}
			else if("video".equals(md.getMedia().getMediaType())) {
				log.debug("Selecting H264 from video media formats...");
				String[] rawSdpLines = rawSdp.split("\n");
				for(String line : rawSdpLines) {
					Matcher m = h264pattern.matcher(line);
					if(m.matches()) {
						log.debug("Found {}, will use media format {}", line, m.group(1));
						ret.getModifiableVideoMediaFormats().add(Integer.parseInt(m.group(1)));
					}
				}
				
				ret.setRemoteVideoPort(md.getMedia().getMediaPort());
				log.debug("remoteVideoPort = {}", ret.getRemoteVideoPort());
			}
		}
		
		return ret;
	}
	
	private static String readResource(String resourceName) {
		InputStream in = SdpHelper.class.getResourceAsStream(resourceName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		try {
			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			
			return sb.toString();
		}
		catch(Exception e) {
			throw new IllegalStateException("Failed to read " + resourceName, e);
		}
		finally {
			try {
				reader.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
	public boolean isUse0port4acceptVideoAsAudio() {
		return use0port4acceptVideoAsAudio;
	}
	public void setUse0port4acceptVideoAsAudio(boolean use0port4acceptVideoAsAudio) {
		this.use0port4acceptVideoAsAudio = use0port4acceptVideoAsAudio;
	}

}
