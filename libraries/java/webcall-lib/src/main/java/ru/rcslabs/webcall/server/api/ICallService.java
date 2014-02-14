package ru.rcslabs.webcall.server.api;

import com.rcslabs.rcl.exception.ServiceNotEnabledException;

import ru.rcslabs.webcall.server.app.IWebcallService;

/**
 * This service provides call functionality.
 * 
 * @author dzernov
 *
 */
public interface ICallService extends IWebcallService {
	
	/**
	 * Opens a client connection.
	 * 
	 * The connection is needed before any calls may be established by
	 * the client. So, this method will be the first in the call flow.
	 * 
	 * @param phone a client phone number
	 * @param password a client password
	 * @param params an additional params
	 * @return a unique UID of this connection to be used in further calls
	 * @throws ServiceNotEnabledException 
	 */
	String open(String phone, String password, Object ... params) throws ServiceNotEnabledException;
	
	/**
	 * Closes a connection with specified UID.
	 */
	void close(String uid);
	
	/**
	 * Starts a call to a specified destination.
	 * 
	 * @param uid a connection UID (see {@link #open(String, String, Object...)})
	 * @param destination a call destination
	 * @param av_params media parameters ("video" or "audio")
	 * @return a call UID to be used in further methods
	 * @throws ServiceNotEnabledException 
	 */
	String call(String uid, String destination, String av_params) throws ServiceNotEnabledException;

	/**
	 * Accepts an incoming call.
	 * 
	 * @param uid a connection UID
	 * @param callId a call UID
	 * @param av_params media parameters ("video" or "audio")
	 */
	void accept(String uid, String callId, String av_params);
	void decline(String uid, String callId);
	void busy(String uid, String callId);
	void unavailable(String uid, String callId);

	void dtmf(String uid, String callId, String dtmf);
	void hangup(String uid, String callId);
	void transfer(String uid, String callId, String destination);
	
	/**
	 * Creates a new conference for the given connection.
	 * 
	 * @param uid a connection UID
	 * @return a conference UID to be used in further calls
	 * @throws ServiceNotEnabledException 
	 */
	String startConference(String uid) throws ServiceNotEnabledException;
	
	/**
	 * Invites a new member to the conference.
	 * 
	 * @param uid a connection UID
	 * @param confId a conference UID
	 * @param phoneNumber a phone number of the invited member
	 */
	void inviteMember(String uid, String confId, String phoneNumber);
	
	/**
	 * Kicks a member from the conference.
	 * 
	 * @param uid a connection UID
	 * @param confId a conference UID
	 * @param phoneNumber a phone number of the kicked member
	 */
	void kickMember(String uid, String confId, String phoneNumber);
	
	/**
	 * Accepts the conference invitation.
	 * 
	 * @param uid a connection UID
	 * @param confId a conference UID
	 */
	void acceptConferenceInvitation(String uid, String confId);
	
	/**
	 * Rejects the conference invitation.
	 * 
	 * @param uid a connection UID
	 * @param confId a conference UID
	 */
	void rejectConferenceInvitation(String uid, String confId);
	
	/**
	 * Finishes the conference
	 * @param uid a connection UID
	 * @param confId a conference UID
	 */
	void finishConference(String uid, String confId);
	
}
