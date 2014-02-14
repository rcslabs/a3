package com.rcslabs.rcl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.event.TelephonyEvent;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;

public class JainSipTelephonyService 
extends AbstractListenable<ITelephonyServiceListener, TelephonyEvent> 
implements ITelephonyService {
	private static Logger log = LoggerFactory.getLogger(ITelephonyService.class);
	
	private final JainSipConnection connection;
	private final JainSipCallManager callManager;

	public JainSipTelephonyService(
			JainSipConnection connection, 
			JainSipCallManager callManager) {
		this.connection = connection;
		this.callManager = callManager;
	}

	@Override
	public List<ICall> getCalls() {
		return new ArrayList<ICall>(callManager.getCallsForConnection(connection));
	}

	@Override
	public ICall newCall() {
		return connection.newCall();
	}
	
	@Override
	public ICall findCall(String id) {
		return callManager.getCallFromPoolById(id);
	}
	
	@Override
	public void fireEvent(TelephonyEvent event) {
		throw new NotImplementedException();
	}

	public void fireEvent(JainSipCall call, TelephonyEvent event) {
		event.setConnection(connection);
		event.setCall(call);
		
		for(ITelephonyServiceListener listener : getListenersCopy()) {
			try {
				event.getEventType().getListenerMethod().invoke(listener, call, event);
			}
			catch(Exception e) {
				log.warn("Got exception from ITelephonyServiceListener", e);
			}
		}
	}
}
