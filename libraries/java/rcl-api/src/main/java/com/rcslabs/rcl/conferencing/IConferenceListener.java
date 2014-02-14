package com.rcslabs.rcl.conferencing;

import com.rcslabs.rcl.conferencing.event.IConferenceEvent;
import com.rcslabs.rcl.conferencing.event.IConferenceMemberEvent;

/**
 * Listener interface for {@link IConference} object.
 *
 */
public interface IConferenceListener {
	
	/**
	 * Upcalled when an error occured.
	 * 
	 * This callback does not mean that the conference
	 * is broken. It just means that some error happened
	 * that needs to be reported somewhere.
	 * 
	 * To get the error information, use event.getErrorInfo().
	 * 
	 * @param event an event
	 */
	void onConferenceError(IConferenceEvent event);
	
	/**
	 * Upcalled when a conference establishing process
	 * has passed some certain stage. May be upcalled
	 * 0 or more times.
	 * 
	 * @param event an event
	 */
	void onConferenceStarting(IConferenceEvent event);
	
	/**
	 * Upcalled when a conference has started successfully.
	 * 
	 * @param event an event
	 */
	void onConferenceStarted(IConferenceEvent event);
	
	/**
	 * Upcalled when a conference has failed to start.
	 * 
	 * @param event an event
	 */
	void onConferenceFailed(IConferenceEvent event);

	/**
	 * Upcalled when a member has joined the conference.
	 * 
	 * @param event an event
	 */
	void onMemberJoined(IConferenceMemberEvent event);
	
	/**
	 * Upcalled when a member has left the conference.
	 * 
	 * @param event an event
	 */
	void onMemberLeft(IConferenceMemberEvent event);
	
	/**
	 * Upcalled when this conference is over.
	 * @param event an event
	 */
	void onConferenceFinished(IConferenceEvent event);
	
	/**
	 * Upcalled when we were kicked from this conference.
	 * 
	 * "We" means a user that corresponds to this connection.
	 * 
	 * @param event an event
	 */
	void onKicked(IConferenceEvent event);
}
