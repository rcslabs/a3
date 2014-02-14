package ru.rcslabs.webcall.server.event.adapter;

import ru.rcslabs.webcall.server.AppDataConstants;
import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.conferencing.IConferenceListener;
import com.rcslabs.rcl.conferencing.IConferenceServiceListener;
import com.rcslabs.rcl.conferencing.event.IConferenceEvent;
import com.rcslabs.rcl.conferencing.event.IConferenceMemberEvent;

public class ConferenceEventListenerAdapter implements IConferenceListener, IConferenceServiceListener {
	public static final ConferenceEventListenerAdapter DEFAULT_INSTANCE = 
			new ConferenceEventListenerAdapter();

	private IClientConnection getCallbackProxy(IConferenceEvent event) {
		return (IClientConnection)event
				.getConference()
				.getCall()
				.getConnection()
				.getApplicationData(AppDataConstants.WEB_CONNECTION);
	}
	
	@Override
	public void onConferenceError(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConferenceStarting(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConferenceStarted(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConferenceFailed(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onMemberJoined(IConferenceMemberEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onMemberLeft(IConferenceMemberEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConferenceFinished(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onKicked(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConferenceInvitation(IConferenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

}
