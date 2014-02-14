package com.rcslabs.rcl.message;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.JainSipCall.TransferNotFoundException;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipPresenceService;
import com.rcslabs.rcl.event.CallTransferEvent;
import com.rcslabs.rcl.event.PresenceEvent;
import com.rcslabs.rcl.presence.IPresenceEvent;
import com.rcslabs.rcl.presence.entity.ContactPresence;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.util.CollectionUtils;
import gov.nist.javax.sip.header.SIPHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.bind.JAXBException;

public class SipNotifyRequestObject extends SipRequestObject {
	private static Logger log = LoggerFactory.getLogger(SipNotifyRequestObject.class);

	public SipNotifyRequestObject(ICallManager callManager) {
		super(callManager);
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void process(RequestEvent event) {
		Request request = event.getRequest();
		final String presentityPhoneNumber = getPhoneNumber(
				((FromHeader)request.getHeader(SIPHeaderNames.FROM)).getAddress()
		);
		EventHeader eventHeader = ((EventHeader)request.getHeader(SIPHeaderNames.EVENT));
		CallIdHeader callIdHeader = (CallIdHeader)request.getHeader(SIPHeaderNames.CALL_ID);
		if(eventHeader == null || callIdHeader == null) {
			sendResponse(event, Response.BAD_REQUEST);
			return;
		}
	
		try {
			sendResponse(event, Response.OK);
			
			byte[] rawContent = request.getRawContent();
			
			if(rawContent != null && "presence".equals(eventHeader.getEventType())) {
				final ContactPresence presence = callManager.getPresenceHelper().unmarshal(rawContent);
				presence.setContactPhoneNumber(presentityPhoneNumber);
				
				callManager.forEachConnectionInPool(new CollectionUtils.Operation<JainSipConnection>() {
					@Override
					public boolean run(JainSipConnection connection) {
						JainSipPresenceService presenceService = connection.getPresenceService();
						if(presenceService.hasSubscription(presentityPhoneNumber)) {
							log.debug("Firing presence event for connection {}", connection.getId());
							presenceService.fireEvent(
									new PresenceEvent(IPresenceEvent.Type.PRESENCE_STATE_CHANGED, presence));
						}
						
						return true;
					}
				});
			}
			else if("refer".equals(eventHeader.getEventType())) {
				JainSipCall call = callManager.getCallFromPool(callIdHeader.getCallId());
				if(call != null) {
					String body = new String(rawContent);
					JainSipCall.Transfer transfer = call.getTransfer(eventHeader.getEventId());
					
					if(body.contains(SipReferRequestObject.NOTIFY_REFER_OK_BODY)) {
						call.fireEvent(
								new CallTransferEvent(
										ICallEvent.Type.CALL_TRANSFERED, 
										transfer.getParams())
						);
						call.finishTransfer(transfer);
					}
					else if(body.contains(SipReferRequestObject.NOTIFY_REFER_FAILED_BODY)) {
						call.fireEvent(
								new CallTransferEvent(
										ICallEvent.Type.TRANSFER_FAILED,
										transfer.getParams()
								)
						);
						call.finishTransfer(transfer);
					}
					else if(!body.contains(SipReferRequestObject.NOTIFY_REFER_TRYING_BODY)) {
						log.warn("Unknown/unsupported NOTIFY status body for refer event");
						call.finishTransfer(transfer);
					}
					//else we got NOTIFY_REFER_TRYING_BODY, and keep waiting, without removing the transfer
				}
				else {
					log.warn("Got refer NOTIFY for call id {}, but couldn't find such call in pool", callIdHeader.getCallId());
				}
			}
		} catch (JAXBException e) {
			log.error("Failed to unmarshal NOTIFY request from presentity " + presentityPhoneNumber, e);
		} catch (TransferNotFoundException e) {
			log.error("Error during call transfer", e);
		}
	}

	@Override
	public ClientTransaction send() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doTimeout(TimeoutEvent event) {}
}
