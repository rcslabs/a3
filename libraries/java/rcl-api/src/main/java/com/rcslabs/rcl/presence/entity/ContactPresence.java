package com.rcslabs.rcl.presence.entity;


public class ContactPresence implements IContactPresence {
	private String contactPhoneNumber;
	private String contactUri;
	private PresenceStatus status;
	private PresenceMood mood;
	private String note;

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	@Override
	public String getPhoneNumber() {
		return contactPhoneNumber;
	}
	
	public void setContactUri(String contactUri) {
		this.contactUri = contactUri;
	}

	@Override
	public String getUri() {
		return contactUri;
	}
	
	public void setMood(PresenceMood mood) {
		this.mood = mood;
	}

	@Override
	public PresenceMood getMood() {
		return mood;
	}
	
	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String getNote() {
		return note;
	}
	
	public void setStatus(PresenceStatus status) {
		this.status = status;
	}

	@Override
	public PresenceStatus getStatus() {
		return status;
	}

}
