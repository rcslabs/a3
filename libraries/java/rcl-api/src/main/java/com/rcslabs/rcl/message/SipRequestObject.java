package com.rcslabs.rcl.message;

import com.rcslabs.rcl.telephony.entity.ICallParameter;
import gov.nist.javax.sip.header.Allow;
import gov.nist.javax.sip.header.AllowList;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.entity.IConnectionParams;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.telephony.entity.ICallParams;

/**
 * Basic class for all SIP request handlers.
 *
 */
public abstract class SipRequestObject extends SipMessageObject {
	private static Logger log = LoggerFactory.getLogger(SipRequestObject.class);
	
	public SipRequestObject(ICallManager callManager) {
		super(callManager);
	}

	/**
	 * Process an incoming request.
	 * 
	 * @param event a request event
	 */
	public abstract void process(RequestEvent event);
	
	/**
	 * Send a request.
	 * 
	 * @return client transaction object for this request
	 * @throws Exception if request fails
	 */
	public abstract ClientTransaction send() throws Exception;
	
	/**
	 * Creates a corresponding response object for this request 
	 * object.
	 * 
	 * @return a newly created response object
	 * @see SipResponseObject
	 */
	protected abstract SipResponseObject doCreateResponseObject();
	
	/**
	 * An action in case of timeout.
	 * 
	 * @param event a timeout event
	 */
	public abstract void doTimeout(TimeoutEvent event);
	
	/**
	 * Returns allowed methods for this request. They get into "Allow"
	 * SIP header.
	 */
	protected Collection<Allow> getAllowedMethods() {
		return null;
	}
	
	public SipResponseObject createResponseObject() {
		SipResponseObject ret = doCreateResponseObject();
		if(ret != null) {
			ret.setRequestObject(this);
		}
		return ret;
	}
	
	/**
	 * Replies with "Method Not Allowed" response.
	 * 
	 * @param event a request event
	 */
	protected void replyMethodNotAllowed(RequestEvent event) {
		try {
			sendResponse(event, Response.METHOD_NOT_ALLOWED);
		} catch (Exception e) {
			log.error("Failed to send response", e);
		}
	}
	
	/**
	 * Sends SIP response. Useful in process() method.
	 * 
	 * @param event a request method
	 * @param responseType a response type
	 */
	protected void sendResponse(RequestEvent event, int responseType) {
		try {
			Response response = callManager.getMessageFactory().createResponse(responseType, event.getRequest());
			ServerTransaction serverTransaction = event.getServerTransaction();
			if(serverTransaction != null) {
				log.debug("sendResponse: {}", response);
				serverTransaction.sendResponse(response);
			}
			else {
				sendResponse(response);
			}
		}
		catch(Exception e) {
			log.error("Failed to respond", e);
		}
	}
	
	/**
	 * Sends SIP response. Useful in process() method.
	 * 
	 * @param response a response to send
	 * @throws Exception if response failed
	 */
	protected void sendResponse(Response response) throws Exception {
		log.debug("Sending response: {}", response);
		callManager.getProvider().sendResponse(response);
	}
	
	/**
	 * Sends SIP response. Useful in process() method.
	 * 
	 * @param tx a server transaction to use
	 * @param response a response to send
	 * @throws Exception if response failed
	 */
	protected void sendResponse(ServerTransaction tx, Response response) throws Exception {
		log.debug("Sending response: {}", response);
		tx.sendResponse(response);
	}
	
	/**
	 * Creates a default SIP request.
	 * 
	 * This request can be afterwards modified with concrete data.
	 * 
	 * @param connection a connection to use
	 * @param requestType a SIP request type
	 * @return a newly creates SIP request
	 * @throws Exception if connection parameters contain errors
	 */
	protected Request createRequest(JainSipConnection connection, String requestType) throws Exception {
		IConnectionParams params = connection.getParams();
		String userName = params.getUserName();
		String phoneNumber = params.getPhoneNumber();
		JainSipGlobalParams globalParams = getGlobalParams();
		AddressFactory addressFactory = callManager.getAddressFactory();
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		
		if(phoneNumber.contains("-")) {
			throw new RclException("Phone number cannot contain hyphens ('-')");
		}

		String sipServerAddress = getSipServerAddress(connection);
		String localAddress = globalParams.getLocalIpAddress();
		int localPort = globalParams.getLocalPort();
		int maxForwards = globalParams.getMaxForwards();

		SipURI serverUri = addressFactory.createSipURI(null, sipServerAddress);

		URI userUri = addressFactory.createSipURI(
				phoneNumber, 
				params.getDomainName() != null ? params.getDomainName() : sipServerAddress
		);
		Address userAddress = addressFactory.createAddress(userName, userUri);

		SipURI userUriLocal = addressFactory.createSipURI(phoneNumber, localAddress);
		userUriLocal.setPort(localPort);
		Address userAddressLocal = addressFactory.createAddress(userName, userUriLocal);

		FromHeader from = headerFactory.createFromHeader(userAddress, UUID.randomUUID().toString());
		ToHeader to = headerFactory.createToHeader(userAddress, null);
		CSeqHeader cSeq = headerFactory.createCSeqHeader(connection.getNextCSeq(), requestType);

		CallIdHeader callId = callManager.getProvider().getNewCallId(); 
		ViaHeader viaHeader = headerFactory.createViaHeader(
				localAddress, 
				localPort, 
				ICallManager.VIA_TRANSPORT, 
				ICallManager.VIA_PREFIX + UUID.randomUUID().toString());
		//viaHeader.setRPort();
		List<ViaHeader> via = Collections.singletonList(viaHeader);
		MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(maxForwards);

		Request req = 
			callManager.getMessageFactory()
			.createRequest(serverUri, requestType, callId, cSeq, from, to, via, maxForwardsHeader);

		//set Contact header
		ContactHeader contact = headerFactory.createContactHeader();
		contact.setAddress(userAddressLocal);
		contact.removeParameter("expires");
		req.addHeader(contact);
		
		//set Allow header
		Collection<Allow> allowedMethods = getAllowedMethods();
		if(allowedMethods != null) {
			AllowList allowList = new AllowList();
			allowList.addAll(allowedMethods);
			req.addHeader(allowList);
		}

		//set User-Agent header
		List<String> userAgentList;
		if(StringUtils.isNotBlank(globalParams.getSipUserAgent())) {
			userAgentList = Collections.singletonList(globalParams.getSipUserAgent());
		}
		else {
			userAgentList = callManager.getVersionHelper().getUserAgentList();
		}
		req.addHeader(
			headerFactory.createUserAgentHeader(
					userAgentList
			)
		);

		return req;
	}
	
	protected String getSipServerAddress(JainSipConnection connection) {
		String ret = connection.getParams().getServerAddress() != null ? 
				connection.getParams().getServerAddress() :
					getGlobalParams().getSipServerAddress();
				
		return getGlobalParams().getSipServerPort() != 0 ?
				ret + ":" + getGlobalParams().getSipServerPort() :
					ret;
	}
	
	protected ClientTransaction sendRequest(Request request) throws Exception {
		log.debug("Sending request: {}", request);
		ClientTransaction tx = callManager.getProvider().getNewClientTransaction(request);
		tx.setApplicationData(this);
		tx.sendRequest();
		
		return tx;
	}
	
	protected ClientTransaction sendRequest(Request request, Dialog dialog) throws Exception {
		log.debug("Sending request: {}", request);
		ClientTransaction tx = callManager.getProvider().getNewClientTransaction(request);
		tx.setApplicationData(this);
		dialog.sendRequest(tx);
		return tx;
	}

	/**
	 * Creates a To: address from the specified parameters.
	 * 
	 * @param callParams a call params, from which to construct an address
	 * @return a resulting address
	 * @throws java.text.ParseException if parameters contain errors
	 */
	protected Address createToAddress(ICallParams callParams) throws ParseException {
        String to = callParams.getTo();
        List<ICallParameter> params = callParams.getSipToParams();
        if(0 != params.size()){
            String _params = ";";
            for(ICallParameter p : params){
                _params += p.getName() + "=" + p.getValue() + ";";
            }
            _params = _params.substring(0, _params.length()-1);
            int i = to.lastIndexOf('>');
            if(-1 != i){
                to = to.substring(0, i) + _params + '>';
            }else{
                to = '<' + to + _params + '>';
            }
        }
        return callManager.getAddressFactory().createAddress(to);
	}

	/**
	 * Creates a From: address from the specified parameters.
	 * 
	 * @param callParams a call params, from which to construct an address
	 * @return a resulting address
	 * @throws java.text.ParseException if parameters contain errors
	 */
	protected Address createFromAddress(ICallParams callParams) throws ParseException {
		return callManager.getAddressFactory().createAddress(callParams.getFrom());
	}
}
