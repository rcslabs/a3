package com.rcslabs.rcl.message;

import com.rcslabs.rcl.*;
import com.rcslabs.util.CollectionUtils;
import gov.nist.javax.sip.header.HeaderExt;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.address.Address;
import javax.sip.message.Request;
import java.util.Collection;

/**
 * Basic class for SIP message hierarchy.
 * 
 * A SIP message is either a request or reponse.
 *
 */
public abstract class SipMessageObject {
	private static Logger log = LoggerFactory.getLogger(SipMessageObject.class);
	
	protected JainSipConnection connection;
	protected ICallManager callManager;
	
	public SipMessageObject(ICallManager callManager) {
		this.callManager = callManager;
	}

	public void setConnection(JainSipConnection connection) {
		this.connection = connection;
	}

	public JainSipConnection getConnection() {
		return connection;
	}

	public JainSipGlobalParams getGlobalParams() {
		return callManager.getGlobalParams();
	}
	
	protected String getPhoneNumber(Address address) {
		//return SipUriUtils.getPhoneNumber(address.getURI().toString());
		return new SipAddressUri(address).getPhoneNumber();
	}
	
	protected String getPhoneNumber(String uri) {
		//return SipUriUtils.getPhoneNumber(uri);
		return new SipAddressUri(uri).getPhoneNumber();
	}
	
	protected String getDomainName(String uri) {
		//return SipUriUtils.getDomainName(uri);
		return new SipAddressUri(uri).getDomainName();
	}
	
	/**
	 * Tries to find a remote call on this server in the connection pool.
	 * 
	 * @param request an incoming SIP (INVITE) request
	 * @param fromPhoneNumber a phone number in From header
	 * @param toPhoneNumber a phone number in To header
	 * @return a call, that is a target of this request on this server, or null,
	 * if there is no such call on this server
	 */
	protected JainSipCall findRemoteCall(Request request, String fromPhoneNumber, String toPhoneNumber) {
		log.info("Trying to find remote call on the same server (from: {}, to: {})", fromPhoneNumber, toPhoneNumber);
		
		JainSipCall remoteCall = null;
		Collection<JainSipConnection> remoteConnections = callManager.findConnectionsByPhone(fromPhoneNumber);
		if (remoteConnections.size()==1) {
			JainSipConnection remoteConnection = remoteConnections.iterator().next();
			
			String rciHeader = callManager.getGlobalParams().getRemoteCallIdentificationHeader();
			if(StringUtils.isNotBlank(rciHeader)) {
				//if the remote call ID header is present, use it to find the call
				final HeaderExt xRemoteCallId = (HeaderExt)request.getHeader(rciHeader);
				if(xRemoteCallId != null) {
					return CollectionUtils.findSingle(
							remoteConnection.getCalls(), 
							new CollectionUtils.Criteria<JainSipCall>() {
								public boolean satisfies(JainSipCall ccall) {
									return ccall.getId().equals(xRemoteCallId.getValue());
								};
							}
					);
				}
			}
			
			for(JainSipCall ccall : remoteConnection.getCalls()) {
				String callToPhoneNumber = ccall.getParams().getTo();
				if(callToPhoneNumber.contains("@")) {
					callToPhoneNumber = callToPhoneNumber.substring(0, callToPhoneNumber.indexOf('@')); 
				}
				
				if(callToPhoneNumber.startsWith("sip:")) {
					callToPhoneNumber = callToPhoneNumber.substring("sip:".length());
				}
				else if(callToPhoneNumber.startsWith("tel:")) {
					callToPhoneNumber = callToPhoneNumber.substring("tel:".length());
				}
				
				//log.debug("Checking call: {} (from: {}, to: {})", new Object[] { ccall.getId(), ccall.getParams().getFromPhoneNumber(), callToPhoneNumber });
				if(toPhoneNumber.equals(callToPhoneNumber) && 
					fromPhoneNumber.equals(ccall.getParams().getFrom())) { //found a corresponding remote call
					log.info("Found remote call on the same server (call id: {})", ccall.getSipId());
					remoteCall = ccall;
					break;
				}
			}
		} else if (remoteConnections.size()==0) {
			// No remote call found
		} else {
			log.info("There are more than one possible remote calls found for call (from: {}, to: {})", fromPhoneNumber, toPhoneNumber);
		}
		
		return remoteCall;
	}

}
