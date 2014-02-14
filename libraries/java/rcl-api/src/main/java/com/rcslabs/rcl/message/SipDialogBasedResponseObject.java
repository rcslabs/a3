package com.rcslabs.rcl.message;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.header.AuthorizationHeader;
import javax.sip.message.Request;

import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.ICallManager;

public abstract class SipDialogBasedResponseObject extends SipResponseObject {

	private final Dialog dialog;

	public SipDialogBasedResponseObject(ICallManager callManager, Dialog dialog) {
		super(callManager);
		this.dialog = dialog;
		if(dialog == null) throw new RclException("No dialog specified (dialog == null)");
	}
	
	@Override
	protected ClientTransaction sendProxyAuthenticateRequest(ResponseEvent event) throws Exception {
		ClientTransaction clientTransaction = event.getClientTransaction();
		Request request = dialog.createRequest(clientTransaction.getRequest().getMethod());
		//getConnection().incRegisterCSeq();
		
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
	
	@Override
	protected ClientTransaction sendWwwAuthenticateRequest(ResponseEvent event) throws Exception {
		ClientTransaction clientTransaction = event.getClientTransaction();
		Request request = dialog.createRequest(clientTransaction.getRequest().getMethod());

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

}
