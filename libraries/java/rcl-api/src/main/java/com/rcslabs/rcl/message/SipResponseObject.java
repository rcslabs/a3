package com.rcslabs.rcl.message;

import java.text.ParseException;
import java.util.Collections;
import java.util.ListIterator;
import java.util.UUID;

import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoolu.sip.authentication.DigestAuthentication;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.event.ConnectionEvent;

/**
 * Basic class for all SIP response handlers.
 *
 */
public abstract class SipResponseObject extends SipMessageObject {
	private static Logger log = LoggerFactory.getLogger(SipResponseObject.class);
	
	private SipRequestObject requestObject;

	public SipResponseObject(ICallManager callManager) {
		super(callManager);
	}
	
	protected void setRequestObject(SipRequestObject requestObject) {
		this.requestObject = requestObject;
	}
	
	public SipRequestObject getRequestObject() {
		return requestObject;
	}
	
	/**
	 * Default handling of response of any type.
	 * 
	 * Normally one needs to override one of the methods, called from
	 * this method.
	 * 
	 * @param event a response event
	 */
	public void doResponse(ResponseEvent event) {
		try {
			int statusCode = event.getResponse().getStatusCode();
			
			// TODO: move SDP into rcl CallEvent
			String sdpAnswerer = null;
			if(0 != event.getResponse().getContentLength().getContentLength()){
                sdpAnswerer = new String(event.getResponse().getRawContent());
			}	
			
			CallIdHeader h = ((CallIdHeader)event.getResponse().getHeader(CallIdHeader.NAME));
			if(null != h){
				String id = h.getCallId();
				if(null != id){
					JainSipCall c = callManager.getCallFromPool(id);
					if(null != c){
						c.getSdpObject().setAnswerer(sdpAnswerer);
					}
				}				
			}
			
			switch(statusCode) {
			case Response.OK:
				doOk(event);
				break;
				
			case Response.ACCEPTED:
				doAccepted(event);
				break;
				
			case Response.UNAUTHORIZED:
				doUnauthorized(event);
				break;
				
			case Response.PROXY_AUTHENTICATION_REQUIRED:
				doProxyAuthenticationRequired(event);
				break;
				
			case Response.TRYING:
				doTrying(event);
				break;
			
			case Response.RINGING:
				doRinging(event);
				break;
				
			case Response.SESSION_PROGRESS:
				doSessionProgress(event);
				break;
				
			default:
				doOther(event);
				break;
			}
		}
		catch(Exception e) {
			log.error("Exception in processResponse", e);
		}
	}

	/**
	 * Handler 200 OK response.
	 * 
	 * @param event a response event
	 */
	protected void doOk(ResponseEvent event) {
	}
	
	/**
	 * Handler for an event, that is not directly supported by this class.
	 * 
	 * @param event a response event
	 */
	protected void doOther(ResponseEvent event) {
		log.warn("Got non-ok response for connection {}: {}, {}", new Object[] { getConnection().getId(), event.getResponse().getStatusCode(), event.getResponse().getReasonPhrase() });
		ConnectionEvent connectionEvent = new ConnectionEvent(IConnectionEvent.Type.CONNECTION_ERROR);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorCode(event.getResponse().getStatusCode());
		errorInfo.setErrorText(event.getResponse().getReasonPhrase());
		connectionEvent.setErrorInfo(errorInfo);
		getConnection().fireEvent(connectionEvent);
	}
	
	/**
	 * Handler for 401 Unauthorized response.
	 * 
	 * @param event a response event
	 */
	protected void doUnauthorized(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		
		log.info("Performing WWW authenticate for client {}", userPhone);
		try {
			sendWwwAuthenticateRequest(event);
		}
		catch(Exception e) {
			log.error("Failed to send WWW Authenticate request", e);
			getConnection().fireErrorEvent("Error while performing WWW Authenticate for user " + userPhone, e);
		}
	}
	
	/**
	 * Handler for 407 Proxy Authentication Required response.
	 * 
	 * @param event a response event
	 */
	protected void doProxyAuthenticationRequired(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		log.info("Performing Proxy authenticate for client {}", userPhone);
		
		try {
			sendProxyAuthenticateRequest(event);
		}
		catch(Exception e) {
			log.error("Failed to send Proxy Authenticate", e);
			getConnection().fireErrorEvent("Error while performing Proxy Authenticate for user " + userPhone, e);
		}
	}
	
	/**
	 * Handler for 100 Trying response.
	 * 
	 * @param event a response event
	 */
	protected void doTrying(ResponseEvent event) {
		doOther(event);
	}
	
	/**
	 * Handler for 180 Ringing response.
	 * 
	 * @param event a response event
	 */
	protected void doRinging(ResponseEvent event) {
		doOther(event);
	}
	
	/**
	 * Handler for 183 Session in Progress response.
	 * 
	 * @param event a response event
	 */
	protected void doSessionProgress(ResponseEvent event) {
		doOther(event);
	}
	
	/**
	 * Handler for 202 Accepted response.
	 * 
	 * @param event a response event
	 */
	protected void doAccepted(ResponseEvent event) {
		doOther(event);
	}
	
	/**
	 * Sends a WWW-Authenticate request.
	 * 
	 * @param event an event object for previous response
	 * @return a client transaction of a sent request
	 * @throws Exception if request failed
	 */
	protected ClientTransaction sendWwwAuthenticateRequest(ResponseEvent event) throws Exception {
		ClientTransaction clientTransaction = event.getClientTransaction();
		Request request = copyRequest(clientTransaction.getRequest()); //clone won't work; sending the same request won't work

		((CSeqHeader)request.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(getConnection().getNextCSeq());
		//getConnection().incRegisterCSeq();

		AuthorizationHeader authHeader = callManager.getHeaderFactory().createAuthorizationHeader(
				generateAuthorizationHeader(
						getConnection(),
						event.getClientTransaction().getRequest(), 
						event.getResponse())
		);
		request.addHeader(authHeader);
		getConnection().setApplicationData(ICallManager.APPDATA_AUTH_HEADER, authHeader);

		return new SipAuthorizedRequestObject(callManager, request, this).send();
	}
	
	/**
	 * Sends a Proxy-Authenticate request.
	 * 
	 * @param event an event object for previous response
	 * @return a client transaction of a sent request
	 * @throws Exception if request failed
	 */
	protected ClientTransaction sendProxyAuthenticateRequest(ResponseEvent event) throws Exception {
		ClientTransaction clientTransaction = event.getClientTransaction();
		Request request = copyRequest(clientTransaction.getRequest());
		
//		((CSeqHeader)request.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(getConnection().getRegisterCSeq());
//		getConnection().incRegisterCSeq();
		((CSeqHeader)request.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(getConnection().getNextCSeq());

		request.addHeader(
				callManager.getHeaderFactory().createProxyAuthorizationHeader(
						generateProxyAuthorizationHeader(
								getConnection(),
								event.getClientTransaction().getRequest(), 
								event.getResponse())
				)
		);

		return new SipAuthorizedRequestObject(callManager, request, this).send();
	}
	
	protected String generateAuthorizationHeader(JainSipConnection connection, Request request, Response response) {
		WWWAuthenticateHeader wwwAuthHeader = (WWWAuthenticateHeader)response.getHeader(WWWAuthenticateHeader.NAME);
		DigestAuthentication digest = new DigestAuthentication(
				request.getMethod(), 
				//request.getRequestURI().toString(),
				"sip:" + getGlobalParams().getSipServerAddress(),
				wwwAuthHeader, 
				null, 
				connection.getParams().getPhoneNumber(), 
				connection.getParams().getPassword());

		return digest.getAuthorizationHeader().getValue();
	}
	
	protected String generateProxyAuthorizationHeader(JainSipConnection connection, Request request, Response response) {
		ProxyAuthenticateHeader authHeader = (ProxyAuthenticateHeader)response.getHeader(ProxyAuthenticateHeader.NAME);
		DigestAuthentication digest = new DigestAuthentication(
				request.getMethod(), 
				//request.getRequestURI().toString(),
				"sip:" + getGlobalParams().getSipServerAddress(),
				authHeader, 
				null, 
				connection.getParams().getPhoneNumber(), 
				connection.getParams().getPassword());
		
		return digest.getProxyAuthorizationHeaderValue();
	}
	
	@SuppressWarnings("unchecked")
	private Request copyRequest(Request origin) throws ParseException, InvalidArgumentException {
		CSeqHeader cSeqHeader = ((CSeqHeader)origin.getHeader(SIPHeaderNames.CSEQ));
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		JainSipGlobalParams globalParams = callManager.getGlobalParams();
		ViaHeader viaHeader = headerFactory.createViaHeader(
				globalParams.getLocalIpAddress(), 
				globalParams.getLocalPort(), 
				ICallManager.VIA_TRANSPORT, 
				ICallManager.VIA_PREFIX + UUID.randomUUID().toString());
		//viaHeader.setRPort();
		
		Request ret = 
			callManager.getMessageFactory().createRequest(
					origin.getRequestURI(),
					origin.getMethod(),
					(CallIdHeader)origin.getHeader(SIPHeaderNames.CALL_ID),
					headerFactory.createCSeqHeader(cSeqHeader.getSeqNumber() + 1, origin.getMethod()),
					(FromHeader)origin.getHeader(SIPHeaderNames.FROM), 
					(ToHeader)origin.getHeader(SIPHeaderNames.TO),
					Collections.singletonList(viaHeader), 
					headerFactory.createMaxForwardsHeader(globalParams.getMaxForwards())
			);
		
		//add headers that do not exist in ret but exist in origin
		for(ListIterator<String> headerNames = origin.getHeaderNames(); headerNames.hasNext();) {
			String headerName = headerNames.next();
			if(!headerName.equals(SIPHeaderNames.AUTHORIZATION) && 
					!headerName.equals(SIPHeaderNames.PROXY_AUTHORIZATION)) 
			{
				if(ret.getHeader(headerName) == null) {
					ListIterator<Header> headers = origin.getHeaders(headerName);
					while(headers.hasNext()) {
						Header header = headers.next();
						ret.addHeader(header);
					}
				}
			}
		}
		
		Object content = origin.getContent();
		if(content != null) {
			ret.setContent(
					content, 
					(ContentTypeHeader)origin.getHeader(SIPHeaderNames.CONTENT_TYPE));
		}
		
		return ret;
	}

	protected long defineExpires(Response response) {
		ExpiresHeader expiresHeader = (ExpiresHeader)response.getHeader(SIPHeaderNames.EXPIRES);
		if(expiresHeader != null) {
			return expiresHeader.getExpires();
		}
		else {
			ContactHeader contactHeader = (ContactHeader)response.getHeader(SIPHeaderNames.CONTACT);
			if(contactHeader != null) {
				int contactExpires = contactHeader.getExpires();
				if(contactExpires > 0) {
					return contactExpires;
				}
			}
		}
		
		return getGlobalParams().getExpires();
	}

}
