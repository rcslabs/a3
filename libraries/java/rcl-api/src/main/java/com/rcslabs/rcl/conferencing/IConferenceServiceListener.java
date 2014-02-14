package com.rcslabs.rcl.conferencing;

import com.rcslabs.rcl.conferencing.event.IConferenceEvent;

/**
 * Listener for a {@link IConferenceService}.
 *
 */
public interface IConferenceServiceListener {

	/**
	 * Upcalled when we have been invited to the conference.
	 * 
	 * @param event an event
	 */
	void onConferenceInvitation(IConferenceEvent event);
}
