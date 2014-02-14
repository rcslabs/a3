package ru.rcslabs.webcall.server.event.adapter;

import ru.rcslabs.webcall.server.AppDataConstants;
import ru.rcslabs.webcall.server.ICallObjectsLifetimeEventsDispatcher;
import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ICallFailedEvent;
import com.rcslabs.rcl.telephony.event.ICallFinishNotificationEvent;
import com.rcslabs.rcl.telephony.event.ICallStartingEvent;
import com.rcslabs.rcl.telephony.event.ICallTransferEvent;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;

public class CallEventListenerAdapter implements ICallListener, ITelephonyServiceListener {
	private final ICallObjectsLifetimeEventsDispatcher dispatcher;

	public CallEventListenerAdapter(ICallObjectsLifetimeEventsDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	private IClientConnection getCallClient(ICallEvent event) {
		return (IClientConnection)event
				.getCall()
				.getConnection()
				.getApplicationData(AppDataConstants.WEB_CONNECTION);
	}
	
	private IClientConnection getCallClient(ITelephonyEvent event) {
		return (IClientConnection)event
				.getConnection()
				.getApplicationData(AppDataConstants.WEB_CONNECTION);
	}
	
	@Override
	public void onCallStarting(ICallStartingEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onCallStarted(ICallEvent event) {
		getCallClient(event).onEvent(event);

	}

	@Override
	public void onCallFinished(ICallEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onCallFailed(ICallFailedEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onCallTransfered(ICallTransferEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onTransferFailed(ICallTransferEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onCallError(ICallEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onIncomingCall(ICall call, ITelephonyEvent event) {
		dispatcher.fireCallCreated(call);
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onCancel(ICall call, ITelephonyEvent event) {
		getCallClient(event).onEvent(event);
	}

	@Override
	public void onCallFinishNotification(ICallFinishNotificationEvent event) {
		getCallClient(event).onEvent(event);
	}

}
