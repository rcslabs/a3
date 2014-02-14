package com.rcslabs.rcl.presence.entity;


/**
 * A presence state of a contact.
 *
 */
public interface IContactPresence {
	/**
	 * Returns a presence URI, eg. sip://7921000000@multifon.ru
	 */
	String getUri();
	
	/**
	 * Returns a phone number.
	 */
	String getPhoneNumber();
	
	/**
	 * Returns a presence status.
	 */
	PresenceStatus getStatus();

	/**
	 * Returns a current mood.
	 */
	PresenceMood getMood();

	/**
	 * Returns a presence note.
	 */
	String getNote();

}
