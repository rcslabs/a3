package com.rcslabs.rcl;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.event.MessagingEvent;
import com.rcslabs.rcl.message.SipMessageRequestObject;
import com.rcslabs.rcl.messaging.IMessage;
import com.rcslabs.rcl.messaging.IMessagingService;
import com.rcslabs.rcl.messaging.IMessagingServiceListener;


public class JainSipMessagingService
extends AbstractListenable<IMessagingServiceListener, MessagingEvent>
implements IMessagingService {
	private final JainSipConnection connection;
	private final ICallManager callManager;
	
	public JainSipMessagingService(JainSipConnection connection, ICallManager callManager) {
		this.connection = connection;
		this.callManager = callManager; 
	}

	@Override
	public void send(IMessage message) {
		try {
			new SipMessageRequestObject(message, connection, callManager, this).send();
		} catch (Exception e) {
			throw new RclException(e);
		}
	}
	
	@Override
	public void fireEvent(MessagingEvent event) {
		event.setConnection(connection);
		super.fireEvent(event);
	}

}
