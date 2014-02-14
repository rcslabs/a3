package com.rcslabs.rcl.conferencing.event;

import com.rcslabs.rcl.conferencing.entity.IConferenceMember;

/**
 * A conference meber-related event.
 *
 */
public interface IConferenceMemberEvent extends IConferenceEvent {

	/**
	 * Returns a conference member that generated this event.
	 */
	IConferenceMember getMember();
}
