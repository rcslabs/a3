package com.rcslabs.rcl.message;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.event.CallFailedEvent;
import com.rcslabs.rcl.event.TelephonyEvent;
import com.rcslabs.rcl.telephony.entity.*;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.SIPHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sdp.SdpException;
import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.Collection;
import java.util.UUID;

public class SipInviteRequestObject extends SipRequestObject {
	private Logger log = LoggerFactory.getLogger(SipInviteRequestObject.class);

	private JainSipCall call;
	private Request request;

	public SipInviteRequestObject(ICallManager callManager, JainSipCall call) {
		super(callManager);
		this.call = call;
		setConnection(call.getConnection());
	}

	public SipInviteRequestObject(ICallManager callManager) {
		super(callManager);
	}
	
	public Request getRequest() {
		return request;
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		if(call == null) throw new IllegalStateException("Call is undefined.");
		return new SipInviteResponseObject(callManager, call);
	}

	@Override
	public void process(RequestEvent event) {
		request = event.getRequest();
		MessageFactory messageFactory = callManager.getMessageFactory();
		
		try {
			ServerTransaction tx = event.getServerTransaction();
			if (tx==null) {
				tx = callManager.getProvider().getNewServerTransaction(event.getRequest());
			}
			//sendResponse(messageFactory.createResponse(Response.TRYING, event.getRequest()));
			sendResponse(tx, messageFactory.createResponse(Response.RINGING, event.getRequest()));
			
			//read request headers
			ToHeader toHeader = (ToHeader)request.getHeader(SIPHeaderNames.TO);
			FromHeader fromHeader = (FromHeader)request.getHeader(SIPHeaderNames.FROM);
			CallIdHeader callIdHeader = (CallIdHeader)request.getHeader(SIPHeaderNames.CALL_ID);
			String toPhoneNumber = getPhoneNumber(toHeader.getAddress());

			//find a corresponding connection in pool
			JainSipConnection connection = null;
			Collection<JainSipConnection> connections = callManager.findConnectionsByPhone(toPhoneNumber);
			if (connections.size()==0) {
				log.warn("Could not find connection for a phone number {}, ignoring INVITE request.", toPhoneNumber);
				sendResponse(messageFactory.createResponse(Response.NOT_FOUND, request));
				return;
			} else if (connections.size()==1) {
				connection = connections.iterator().next();
			} else {
				// TODO: Process incoming invite for all connections !
				log.warn("There are more than one connection for a phone number {}, ignoring INVITE request.", toPhoneNumber);
				sendResponse(messageFactory.createResponse(Response.NOT_FOUND, request));
				return;
			}

            String sdpOfferer = new String(request.getRawContent());

    		try {

                // FIXME: CallParams(from, to)  set INCOMING call type
				CallParams callParams = new CallParams(fromHeader.getAddress().toString(), toHeader.getAddress().toString());
                ISdpObject sdp = new SdpObject();
                sdp.setOfferer(sdpOfferer);
                callParams.setSdpObject(sdp);

				//create new call and set all the needed data
				JainSipCall call = connection.newCall(callParams);
				call.setCurrentRequest(event.getRequest());
				// ServerTransaction tx = callManager.getProvider().getNewServerTransaction(event.getRequest());
				call.setInviteServerTransaction(tx);
				call.setSipId(callIdHeader.getCallId());
				call.setAccepted(true);
				call.setInviteRequestObject(this);
				callManager.addCallToPool(call.getSipId(), call);
				
				this.call = call;
				setConnection(connection);
				
				//special trick for conferencing with Teligent
				boolean isFocus = (
						((ContactHeader)event
								.getRequest()
								.getHeader(SIPHeaderNames.CONTACT)
						).getParameter("isFocus") != null
				);
				if(!isFocus) {
					sendResponse(tx, messageFactory.createResponse(Response.RINGING, event.getRequest()));
				}
				else {
					log.warn("Not sending RINGING response, because 'isFocus' parameter in 'Contact' header is present");
				}

                getConnection().getTelephonyService()
                        .fireEvent(call, new TelephonyEvent(ITelephonyEvent.Type.INCOMING_CALL));
			}
			catch(SdpException e) {
				connection.fireErrorEvent("Internal error: failed to parse SDP: " + sdpOfferer, e);
			}
			catch(Exception e) {
				connection.fireErrorEvent("Error: failed to respond to incoming INVITE request", e);
			}
		}
		catch(Exception e) {
			log.error("Exception at processInviteRequest(): ", e);
		}
	}

	@Override
	public ClientTransaction send() throws Exception {
		if(call == null) throw new IllegalStateException("Call is undefined.");
		
		if(call.getCurrentDialog() != null) {
			throw new IllegalStateException("Call already started.");
		}
		
		//create an INVITE request
		Request inviteRequest = createRequest(call.getConnection(), Request.INVITE);
		
		// Override FROM header
		if ((call.getParams().getFrom()!=null & call.getParams().getFrom()!=call.getConnection().getParams().getPhoneNumber()) |
			(call.getParams().getFrom()!=null & call.getParams().getFrom()!=call.getConnection().getParams().getUserName())) {
			Address fromAddress = createFromAddress(call.getParams());
			((FromHeader)inviteRequest.getHeader(SIPHeaderNames.FROM)).setAddress(fromAddress);
		}
		
		//set Expires header
		ExpiresHeader expires = inviteRequest.getExpires();
		if (expires==null) {
			expires = new Expires();
			inviteRequest.setExpires(expires);
		}
		expires.setExpires(3600);
		
		//set To header
		Address toAddress = createToAddress(call.getParams());
		((ToHeader)inviteRequest.getHeader(SIPHeaderNames.TO)).setAddress(toAddress);
		
		//set request URI
		inviteRequest.setRequestURI(toAddress.getURI());

		//set Call-Id header
		if(null != call.getId()){ // call ID already created on a call instance
			((CallIdHeader)inviteRequest.getHeader(SIPHeaderNames.CALL_ID)).setCallId(call.getId());
		}else{ // copy call ID from SIP header into a call instance
			call.setSipId(((CallIdHeader)inviteRequest.getHeader(SIPHeaderNames.CALL_ID)).getCallId());
		}

		//request body
        inviteRequest.setContent(
                call.getSdpObject().getOfferer(),
                callManager.getHeaderFactory().createContentTypeHeader("application", "sdp")
        );
		
		//custom headers if present
        for(ICallParameter p : call.getParams().getSipXHeaders()){
            inviteRequest.addHeader(
                    callManager.getHeaderFactory().createHeader(p.getName(), p.getValue().toString())
            );
        }

		ClientTransaction tx = sendRequest(inviteRequest);
		call.setInviteClientTransaction(tx);
		
		callManager.addCallToPool(call.getSipId(), call);
		
		return tx;
	}

	public void accept() throws Exception {
		if(call == null) throw new IllegalStateException("Call is undefined.");
		
		//create OK response
		Response response = callManager.getMessageFactory().createResponse(Response.OK, call.getCurrentRequest());
		((ToHeader)response.getHeader(SIPHeaderNames.TO)).setTag(UUID.randomUUID().toString());
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		AddressFactory addressFactory = callManager.getAddressFactory();

		response.setContent(
                call.getSdpObject().getAnswerer(),
				headerFactory.createContentTypeHeader("application", "sdp"));	

		Address userAddressLocal = addressFactory.createAddress(call.getParams().getFrom());
		
		ContactHeader contact = headerFactory.createContactHeader();
		contact.setAddress(userAddressLocal);
		response.addHeader(contact);
		
		ServerTransaction transaction = call.getInviteServerTransaction();
		log.debug("Sending response: {}", response);
		transaction.sendResponse(response);
	}
	
	public void reject(RejectReason reason) throws Exception
    {
		if(call == null) throw new IllegalStateException("Call is undefined.");
		
		MessageFactory messageFactory = callManager.getMessageFactory();
		AddressFactory addressFactory = callManager.getAddressFactory();
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		
		Response response;
		switch(reason) {
		case BUSY:
			response = messageFactory.createResponse(Response.BUSY_HERE, call.getCurrentRequest());
			break;
			
		case UNAVAILABLE:
			response = messageFactory.createResponse(Response.TEMPORARILY_UNAVAILABLE, call.getCurrentRequest());
			break;
			
		case ERROR:
			response = messageFactory.createResponse(Response.SERVER_INTERNAL_ERROR, call.getCurrentRequest());
			break;
		
		case DECLINE:
		default:
			response = messageFactory.createResponse(Response.DECLINE, call.getCurrentRequest());
			break;
		}

		Address userAddressLocal = addressFactory.createAddress(call.getParams().getFrom());
		
		ContactHeader contact = headerFactory.createContactHeader();
		contact.setAddress(userAddressLocal);
		response.addHeader(contact);
		
		ServerTransaction transaction = call.getInviteServerTransaction(); //provider.getNewServerTransaction(call.getCurrentRequest());
		log.debug("Sending response: {}", response);
		transaction.sendResponse(response);
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		callManager.removeCallFromPool(call.getSipId());
		
		CallEvent callEvent = new CallFailedEvent(RejectReason.UNAVAILABLE);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorText("Timeout");
		callEvent.setErrorInfo(errorInfo);
		call.fireEvent(callEvent);
	}
}
