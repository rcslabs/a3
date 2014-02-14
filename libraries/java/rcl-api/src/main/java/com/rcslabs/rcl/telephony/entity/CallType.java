package com.rcslabs.rcl.telephony.entity;

import java.util.EnumSet;

import com.rcslabs.rcl.exception.UnsupportedMediaException;

/**
 * Type of a call.
 *
 */
public enum CallType {
	AUDIO,
	VIDEO;
	
	private static EnumSet<CallType> all = EnumSet.allOf(CallType.class);
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}

	public static CallType fromString(String s) {
		for(CallType ct : all) {
			if(ct.toString().equalsIgnoreCase(s)) return ct;
		}
		throw new UnsupportedMediaException("Unsupported media type: " + s);
	}
}
