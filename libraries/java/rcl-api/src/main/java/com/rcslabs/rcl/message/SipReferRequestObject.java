package com.rcslabs.rcl.message;

import com.rcslabs.rcl.telephony.entity.CallParameterSipTo;
import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ReferToHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.event.CallTransferEvent;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.entity.CallParams;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ICallEvent.Type;
import com.rcslabs.rcl.telephony.event.ICallFailedEvent;
import com.rcslabs.rcl.telephony.event.ICallFinishNotificationEvent;
import com.rcslabs.rcl.telephony.event.ICallStartingEvent;
import com.rcslabs.rcl.telephony.event.ICallTransferEvent;

public class SipReferRequestObject extends SipRequestObject implements ICallListener {
	public static final String NOTIFY_REFER_TRYING_BODY = "SIP/2.0 100 Trying";
	public static final String NOTIFY_REFER_OK_BODY = "SIP/2.0 200 OK";
	public static final String NOTIFY_REFER_FAILED_BODY = "SIP/2.0 503 Service Unavailable";
	private static final int SUBSCRIPTION_EXPIRES = 180;
	
	private final ICallParams referToParams;
	private final JainSipCall call;

	public SipReferRequestObject(
			ICallManager callManager,
			JainSipCall call,
			ICallParams referToParams) 
	{
		super(callManager);
		this.call = call;
		this.referToParams = referToParams.clone();
		setConnection(call.getConnection());
	}

	@Override
	public void process(RequestEvent event) {
		Request request = event.getRequest();
		ReferToHeader referToHeader = (ReferToHeader)request.getHeader(ReferToHeader.NAME);
		if(referToHeader == null) {
			sendResponse(event, Response.BAD_REQUEST);
			return;
		}
		
		sendResponse(event, Response.ACCEPTED);
		
		try {
			Dialog dialog = call.getCurrentDialog();
			sendRequest(createNotifyRequest(dialog, NOTIFY_REFER_TRYING_BODY), dialog);
			
			JainSipCall newCall = getConnection().newCall();
			newCall.addListener(this);
			CallParams params = new CallParams("FIXME: set from on REQUEST", getPhoneNumber(referToHeader.getAddress()));
			newCall.start(params);
		}
		catch(Exception e) {
			call.fireErrorEvent("Failed to transfer call", e);
		}
	}

	private Request createNotifyRequest(Dialog dialog, String body) throws Exception {
		Request request = dialog.createRequest(Request.NOTIFY); //createRequest(getConnection(), Request.NOTIFY);
		((CSeqHeader)request.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(
				dialog.getLocalSeqNumber() + 1
		);

		HeaderFactory headerFactory = callManager.getHeaderFactory();
		
		//add Event header 
		request.addHeader(
				headerFactory.createEventHeader("refer")
		);
		
		//add Subscription-State header
		{
			SubscriptionStateHeader header = headerFactory.createSubscriptionStateHeader("active");
			header.setExpires(SUBSCRIPTION_EXPIRES);
			request.addHeader(header);
		}
		
		//add body
		request.setContent(
				body, 
				headerFactory.createContentTypeHeader("message", "sipfrag")
		);
		
		return request;
	}

	@Override
	public ClientTransaction send() throws Exception {
		Dialog dialog = call.getCurrentDialog();
		if(dialog == null) {
			throw new RclException(
					String.format(
						"No current dialog, so, no call (not started or already finished). Call ID: %s. Connection ID: %s", 
						call.getSipId(),
						call.getConnection().getId()
					)
			);
		}
		
		Request request = dialog.createRequest(Request.REFER); //createRequest(getConnection(), Request.REFER);
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		
		long cseq = dialog.getLocalSeqNumber() + 1;
		((CSeqHeader)request.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(cseq);
        referToParams.addParameter(new CallParameterSipTo("method", "INVITE"));
		request.addHeader(
				headerFactory.createReferToHeader( createToAddress(referToParams))
		);
		call.startTransfer(new JainSipCall.Transfer(cseq, referToParams));
		
		return sendRequest(request, dialog);
	}

	@Override
	protected SipResponseObject doCreateResponseObject() {
		if(call == null) throw new IllegalStateException("Call is undefined.");
		return new SipReferResponseObject(callManager, call, referToParams);
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		CallEvent callEvent = new CallTransferEvent(Type.TRANSFER_FAILED, referToParams);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorText("Timeout");
		callEvent.setErrorInfo(errorInfo);
		call.fireEvent(callEvent);
	}

	@Override
	public void onCallStarting(ICallStartingEvent event) {
		try {
			Dialog dialog = call.getCurrentDialog();
			sendRequest(
					createNotifyRequest(dialog, NOTIFY_REFER_TRYING_BODY),
					dialog
			);
		} catch (Exception e) {
			throw new RclException(e);
		}
	}

	@Override
	public void onCallStarted(ICallEvent event) {
		try {
			Dialog dialog = call.getCurrentDialog();
			sendRequest(
					createNotifyRequest(dialog, NOTIFY_REFER_OK_BODY),
					dialog
			);
		} catch (Exception e) {
			throw new RclException(e);
		}
	}

	@Override
	public void onCallFinished(ICallEvent event) {
	}

	@Override
	public void onCallFailed(ICallFailedEvent event) {
		CallTransferEvent transferEvent = new CallTransferEvent(
				ICallEvent.Type.TRANSFER_FAILED, 
				referToParams
		);
		ErrorInfo einfo = new ErrorInfo();
		einfo.setErrorText("Call failed: " + event.getRejectReason());
		onTransferFailed(transferEvent);
	}

	@Override
	public void onCallTransfered(ICallTransferEvent event) {
	}

	@Override
	public void onTransferFailed(ICallTransferEvent event) {
		try {
			call.fireEvent(
					new CallTransferEvent(ICallEvent.Type.TRANSFER_FAILED, event.getTransferParams())
			);
			Dialog dialog = call.getCurrentDialog();
			sendRequest(
					createNotifyRequest(dialog, NOTIFY_REFER_FAILED_BODY),
					dialog
			);
		} catch (Exception e) {
			throw new RclException(e);
		}
	}

	@Override
	public void onCallError(ICallEvent event) {
		call.fireEvent((CallEvent)event);
	}

	@Override
	public void onCallFinishNotification(ICallFinishNotificationEvent event) {
	}

}
