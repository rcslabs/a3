package com.rcslabs.rcl.telephony.entity;

import java.util.EnumSet;

public enum MediaType {
	AUDIO,
	VIDEO, 
	DIRECT_FLASH_AUDIO,
	DIRECT_FLASH_VIDEO;
	
	public boolean isDirectFlash() {
		return EnumSet.of(DIRECT_FLASH_AUDIO, DIRECT_FLASH_VIDEO).contains(this);
	}
	
	public boolean isVideo() {
		return EnumSet.of(DIRECT_FLASH_VIDEO, VIDEO).contains(this);
	}
	
	public MediaType toDirectFlash() {
		switch(this) {
		case AUDIO:
			return DIRECT_FLASH_AUDIO;
			
		case VIDEO:
			return DIRECT_FLASH_VIDEO;
			
		default:
			return this;
		}
	}
}
