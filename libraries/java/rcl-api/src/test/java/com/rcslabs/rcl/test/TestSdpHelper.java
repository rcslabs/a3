package com.rcslabs.rcl.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.SdpHelper;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.entity.CallParams;
import com.rcslabs.rcl.telephony.entity.CallType;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.entity.MediaParams;
import com.rcslabs.rcl.telephony.entity.RejectReason;

public class TestSdpHelper {

	@Test
	public void testH264PayloadTypeDefault() throws Exception {
		SdpHelper helper = new SdpHelper();
		String sdp = helper.generateSdp(createICall(CallType.VIDEO), createMediaParams());
		String videoLine = findLineStartingBy(sdp, "m=video");
		Assert.assertEquals("m=video 0 RTP/AVP 96", videoLine);
		findLineStartingBy(sdp, "a=rtpmap:96 H264/90000");
	}

	@Test
	public void testH264PayloadTypeCustom() throws Exception {
		SdpHelper helper = new SdpHelper();
		helper.setH264PayloadType(103);
		String sdp = helper.generateSdp(createICall(CallType.VIDEO), createMediaParams());
		String videoLine = findLineStartingBy(sdp, "m=video");
		Assert.assertEquals("m=video 0 RTP/AVP 103", videoLine);
		findLineStartingBy(sdp, "a=rtpmap:103 H264/90000");
	}

	@Test
	public void testH264PayloadTypeIncomingNonDefault() throws Exception {
		SdpHelper helper = new SdpHelper();
		MediaParams media = createMediaParams();
		Set<Integer> videoFormats = new HashSet<Integer>();
		videoFormats.add(111);
		media.setVideoMediaFormats(videoFormats);
		String sdp = helper.generateSdp(createICall(CallType.VIDEO), media);
		String videoLine = findLineStartingBy(sdp, "m=video");
		Assert.assertEquals("m=video 0 RTP/AVP 111", videoLine);
		findLineStartingBy(sdp, "a=rtpmap:111 H264/90000");
	}

	@Test
	public void testH264PayloadTypeIncomingCustomNonDefault() throws Exception {
		SdpHelper helper = new SdpHelper();
		helper.setH264PayloadType(103);
		MediaParams media = createMediaParams();
		Set<Integer> videoFormats = new HashSet<Integer>();
		videoFormats.add(111);
		media.setVideoMediaFormats(videoFormats);
		String sdp = helper.generateSdp(createICall(CallType.VIDEO), media);
		String videoLine = findLineStartingBy(sdp, "m=video");
		Assert.assertEquals("m=video 0 RTP/AVP 111", videoLine);
		findLineStartingBy(sdp, "a=rtpmap:111 H264/90000");
	}

	private static String findLineStartingBy(String sdp, String prefix) {
		String[] sdpsplitted = sdp.split("\n");
		String foundLine = null;
		for (String line : sdpsplitted) {
			if (line.startsWith(prefix)) {
				if (foundLine!=null) {
					throw new AssertionError("Several lines starting with '"+prefix+"' are found in '"+sdp+"'");
				}
				foundLine = line;
			}
		}
		if (foundLine==null) {
			throw new AssertionError("No line starting with '"+prefix+"' is found in '"+sdp+"'");
		}
		return foundLine;
	}
	
	private MediaParams createMediaParams() {
		return new MediaParams();
	}
	
	private TestCall createICall(CallType callType) {
		TestCall call = new TestCall();
		call.params.setFrom("1234");
		call.params.setCallType(callType);
		return call;
	}
	
	private class TestCall implements ICall {
		private CallParams params = new CallParams();
		
		@Override
		public void addListener(ICallListener listener) {}

		@Override
		public void removeListener(ICallListener listener) {}

		@Override
		public ICallParams getParams() {return params;}

		@Override
		public void setApplicationData(Object key, Object data) {}

		@Override
		public Object getApplicationData(Object key) {return null;}

		@Override
		public void removeApplicationData(Object key) {}

		@Override
		public void start(ICallParams params) {}

        @Override
        public void accept(CallType callType, String sdpAnswerer) {}

		@Override
		public void accept(CallType callType) {}

		@Override
		public void reject(RejectReason reason) {}

		@Override
		public void finish() {}

		@Override
		public IConnection getConnection() {return null;}

		@Override
		public String getId() {return null;}

		@Override
		public void dtmf(String digits) {}

		@Override
		public void transfer(ICallParams toParams) {}
		
	}

}
