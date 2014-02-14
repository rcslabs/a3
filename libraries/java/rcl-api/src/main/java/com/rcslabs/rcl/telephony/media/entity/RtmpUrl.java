package com.rcslabs.rcl.telephony.media.entity;

import java.util.Arrays;

/**
 * An RTMP URL.
 * 
 * This class holds 2 versions of a URL:
 * <ul>
 * 	<li>an internal, for server - agent interaction;</li>
 * 	<li>an external, for server - client interaction.</li>
 * </ul>
 * 
 * An external URL may also have several versions for several possible
 * interaction protocols. Client may wish to try each or to take the first
 * one that works (implementations may vary).
 *
 */
public class RtmpUrl {
	private String internal;
	private String[] external;
	
	public RtmpUrl(String internal, String[] external) {
		this.internal = internal;
		this.external = Arrays.copyOf(external, external.length);
	}
	
	public RtmpUrl(String internal, String external) {
		this.internal = internal;
		this.external = new String[] { external };
	}

	/**
	 * Returns an internal version of this URL.
	 */
	public String getInternal() {
		return internal;
	}

	/**
	 * Returns all external versions of this URL.
	 */
	public String[] getExternal() {
		return Arrays.copyOf(external, external.length);
	}
	
	/**
	 * Returns all external versions of this URL in one single string, space separated.
	 */
	public String getExternalOneString() {
		StringBuilder sb = new StringBuilder();
		for(String s : external) {
			sb.append(s).append(" ");
		}
		
		sb.setLength(sb.length() - 1); //cut last space
		return sb.toString();
	}

	@Override
	public String toString() {
		return String.format("RtmpUrl [internal=%s, external=%s]", internal,
				Arrays.toString(external));
	}

}
