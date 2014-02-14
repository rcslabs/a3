package com.rcslabs.rcl.presence;

import java.util.List;

import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.core.IService;
import com.rcslabs.rcl.presence.entity.IContactPresence;
import com.rcslabs.rcl.presence.entity.PresenceMood;
import com.rcslabs.rcl.presence.entity.PresenceStatus;

/**
 * Provides presence-related functionality.
 *
 */
public interface IPresenceService 
extends IService, IContactPresence, IListenable<IPresenceListener> {

	/**
	 * Sets the own status of the connected user.
	 * @param status a new status
	 */
	void setStatus(PresenceStatus status);
	
	/**
	 * Sets the own mood of the connected user.
	 * @param mood a new mood
	 */
	void setMood(PresenceMood mood);
	
	/**
	 * Sets the own presence note of the connected user.
	 * @param note a new note
	 */
	void setNote(String note);
	
	/**
	 * Creates a new subscription to the given contacts' presence.
	 * @param phoneNumbers phone numbers of the contacts, to which we subscribe
	 */
	void subscribeToContacts(String[] phoneNumbers);
	
	/**
	 * Remove subscriptions to the given contacts.
	 * @param phoneNumbers phone numbers of the contacts, which we unsubscribe
	 */
	public void unsubscribeContacts(String[] phoneNumbers);
	
	/**
	 * Returns the presence of all the contacts, to which we have subscribed.
	 * @return
	 */
	List<IContactPresence> getContactStates();
	
	/**
	 * Removes all presence subscriptions.
	 */
	void unsubscribe();
}
