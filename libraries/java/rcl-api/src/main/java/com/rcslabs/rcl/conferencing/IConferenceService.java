package com.rcslabs.rcl.conferencing;

import java.util.Collection;

import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.core.IService;
import com.rcslabs.rcl.telephony.ICall;

/**
 * A service to access conferencing functionality.
 *
 */
public interface IConferenceService extends IService, IListenable<IConferenceServiceListener> {

	/**
	 * Creates new conference object, which can later be used to
	 * establish a conference.
	 */
	IConference newConference();
	
	/**
	 * Finds an existing conference for a given call, if such exists.
	 *  
	 * @param call a call for which to find a conference
	 * @return a conference object or null, if there is no conference
	 * for this call
	 */
	IConference findConferenceForCall(ICall call);
	
	/**
	 * Lists all the conferences available on this server.
	 * 
	 * @return a list of available conferences (empty list if no conferences
	 * available)
	 */
	Collection<IConference> listConferences();

	/**
	 * Finds conference by specified UID.
	 * 
	 * @param id conference id
	 * @return an existing conference or null, if the conference does not exist
	 */
	IConference findConference(String id);
}
