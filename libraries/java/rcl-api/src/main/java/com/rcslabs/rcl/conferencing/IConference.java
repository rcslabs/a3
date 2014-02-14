package com.rcslabs.rcl.conferencing;

import java.util.Collection;

import com.rcslabs.rcl.conferencing.entity.IConferenceMember;
import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.telephony.ICall;

/**
 * A conference.
 *
 */
public interface IConference extends IListenable<IConferenceListener> {
	
	/**
	 * Returns a unique conference id.
	 */
	String getId();

	/**
	 * Returns a call that corresponds to this conference.
	 * A call is returned for this connection.
	 * 
	 * @param member a member, for which to return a call
	 */
	ICall getCall();
	
	/**
	 * Starts the conference.
	 */
	void start();
	
	/**
	 * Finishes the conference.
	 */
	void finish();
	
	/**
	 * Invites a member to this conference.
	 * 
	 * @param member a member to invite
	 */
	void inviteMember(IConferenceMember member);
	
	/**
	 * Kicks a member from this conference.
	 * 
	 * @param member a member to kick
	 */
	void kickMember(IConferenceMember member);
	
	/**
	 * Leave this conference.
	 */
	void leave();
	
	/**
	 * Lists members of this conference.
	 * 
	 * @param includeMyself whether to include this user in a returned list
	 */
	Collection<IConferenceMember> listMembers(boolean includeMyself);
	
	/**
	 * Accept an invitation to this conference.
	 */
	void acceptInvitation();
	
	/**
	 * Reject an invitation to this conference.
	 */
	void rejectInvitation();
}
