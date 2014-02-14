package com.rcslabs.rcl;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.exception.RclCheckedException;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.message.SipInviteRequestObject;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.entity.*;
import com.rcslabs.rcl.telephony.event.ICallEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;
import java.util.*;

public class JainSipCall
extends AbstractListenable<ICallListener, CallEvent> 
implements ICall, ICallParams
{
	private static Logger log = LoggerFactory.getLogger(JainSipCall.class);
	protected final JainSipConnection connection;
	protected final JainSipCallManager callManager;
	protected ICallParams params;
	private Dialog currentDialog;
	private Request currentRequest;
	private Map<Object, Object> appData = new HashMap<Object, Object>();
	private String sipId;
	private ClientTransaction inviteClientTransaction;
	private ServerTransaction inviteServerTransaction;
	protected volatile boolean accepted = false;
	private SipInviteRequestObject inviteRequestObject;
	private List<Transfer> transfers = Collections.synchronizedList(new ArrayList<Transfer>());
	private String id;// = UUID.randomUUID().toString();

	public static class Transfer {

		private final long id;
		private final ICallParams params;

		public Transfer(long id, ICallParams params) {
			this.id = id;
			this.params = params.clone();
		}

		public long getId() {
			return id;
		}
		
		public ICallParams getParams() {
			return params;
		}
	}
	
	public static class TransferNotFoundException extends RclCheckedException {
		private static final long serialVersionUID = -4198209136762225820L;

		public TransferNotFoundException(String message) {
			super(message);
		}
		
	}

	public JainSipCall(JainSipConnection connection, JainSipCallManager callManager) {
		this.connection = connection;
		this.callManager = callManager;
	}
	
	public JainSipCall(JainSipConnection connection, JainSipCallManager callManager, CallParams params) {
		this.connection = connection;
		this.callManager = callManager;
		this.params = params;
	}

	@Override
	public void start(ICallParams params) {
		try {
            this.params = params;
			callManager.invite(this);
		} catch (Exception e) {
			throw new RclException("Invite failed for user " + getConnection().getParams().getPhoneNumber(), e);
		}
	}

	@Override
    public void accept(CallType callType, String sdpAnswerer) {
		log.debug("JainSipCall accept SDP: " + sdpAnswerer);
        params.getSdpObject().setAnswerer(sdpAnswerer);
        accept(callType);
	}
	
	@Override
	public void accept(CallType callType)
    {
        if(inviteRequestObject == null){
            throw new IllegalStateException("No incoming call. Nothing to accept.");
        }

        try {
		    params.setCallType(callType);
            inviteRequestObject.accept();
        } catch (RclException e) {
            throw e;
        } catch (Exception e) {
            throw new RclException("Accept failed for user " + getConnection().getParams().getPhoneNumber(), e);
        }
	}
	
	@Override
	public void reject(RejectReason reason) {
		if(inviteRequestObject == null) throw new IllegalStateException("No incoming call. Nothing to reject.");
		
		try {
			//callManager.handleIncomingCall(this, false, reason);
			inviteRequestObject.reject(reason);
		}
		catch (RclException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RclException("Reject failed for user " + getConnection().getParams().getPhoneNumber(), e);
		}
	}

	@Override
	public void finish() {
		try {
			if(isAccepted()) {
				callManager.bye(this);
			} else {
				callManager.cancel(this);
			}
		}
		catch (RclException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RclException("Bye failed for user " + getConnection().getParams().getPhoneNumber(), e);
		}
	}
	
	@Override
	public void transfer(ICallParams toParams) {
		if(toParams == null) throw new IllegalArgumentException("toParams cannot be null");
		
		try {
			callManager.transfer(this, toParams);
		}
		catch(RclException e) {
			throw e;
		}
		catch(Exception e) {
			throw new RclException("Transfer failed for call with id " + sipId, e);
		}
	}

	public ICallParams getParams() {
		return this;
	}

	public JainSipConnection getConnection() {
		return connection;
	}
	
	public String getId() {
		return id;
	}
	
	public void fireEvent(CallEvent callEvent) {
		callEvent.setCall(this);
		super.fireEvent(callEvent);
	}

	public void fireErrorEvent(String errorText, Exception cause) {
		log.debug("Firing error: " + errorText, cause);
		CallEvent event = new CallEvent(Type.CALL_ERROR);
		ErrorInfo errInfo = new ErrorInfo();
		errInfo.setErrorText(errorText);
		errInfo.setCause(new RclException(cause));
		event.setErrorInfo(errInfo);
		fireEvent(event);
	}
	
	public void fireErrorEvent(int statusCode, String errorText) {
		log.debug("Firing error: {}: {}", statusCode, errorText);
		CallEvent event = new CallEvent(Type.CALL_ERROR);
		ErrorInfo errInfo = new ErrorInfo();
		errInfo.setErrorCode(statusCode);
		errInfo.setErrorText(errorText);
		event.setErrorInfo(errInfo);
		fireEvent(event);
	}

	public synchronized void setCurrentDialog(Dialog currentDialog) {
		this.currentDialog = currentDialog;
	}

	public synchronized Dialog getCurrentDialog() {
		return currentDialog;
	}

	public void setCurrentRequest(Request currentRequest) {
		this.currentRequest = currentRequest;
	}

	public Request getCurrentRequest() {
		return currentRequest;
	}

	public Object getApplicationData(Object key) {
		return appData.get(key);
	}

	public void setApplicationData(Object key, Object data) {
		appData.put(key, data);		
	}
	
	public void removeApplicationData(Object key) {
		appData.remove(key);
	}

	public void setSipId(String id) {
		this.id = this.sipId = id;
	}

	public String getSipId() {
		return sipId;
	}

	public void setInviteClientTransaction(ClientTransaction inviteClientTransaction) {
		this.inviteClientTransaction = inviteClientTransaction;
	}

	public ClientTransaction getInviteClientTransaction() {
		return inviteClientTransaction;
	}

	public void setInviteServerTransaction(ServerTransaction inviteServerTransaction) {
		this.inviteServerTransaction = inviteServerTransaction;
	}

	public ServerTransaction getInviteServerTransaction() {
		return inviteServerTransaction;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setInviteRequestObject(SipInviteRequestObject inviteRequestObject) {
		this.inviteRequestObject = inviteRequestObject;
	}

	public SipInviteRequestObject getInviteRequestObject() {
		return inviteRequestObject;
	}

	@Override
	public void dtmf(String digits) {
			throw new RclException("Cannot send DTMF: no media call.");
	}

    @Override
    public String getTo() {
        return params.getTo();
    }

    @Override
    public void setTo(String value) {
        params.setTo(value);
    }

    @Override
    public void setTo(String name, String uri) {
        params.setTo(name, uri);
    }

    @Override
    public String getFrom() {
        return params.getFrom();
    }

    @Override
    public void setFrom(String value) {
        params.setFrom(value);
    }

    @Override
    public void setFrom(String name, String uri) {
        params.setFrom(name, uri);
    }

    @Override
	public CallType getCallType() {
		return params.getCallType();
	}

    @Override
    public void setCallType(CallType type) {
        params.setCallType(type);
    }

    @Override
    public ISdpObject getSdpObject() {
        return params.getSdpObject();
    }

    @Override
    public void setSdpObject(ISdpObject value) {
        params.setSdpObject(value);
    }

    @Override
    public void addParameter(ICallParameter param) {
        params.addParameter(param);
    }

    @Override
	public ICallParams clone() {
		return params.clone();
	}

    @Override
    public List<ICallParameter> getSipXHeaders() {
        return params.getSipXHeaders();
    }

    @Override
    public List<ICallParameter> getSipToParams() {
        return params.getSipToParams();
    }

    public void startTransfer(Transfer transfer) {
		transfers.add(transfer);
	}

	public Transfer getTransfer(String id) throws TransferNotFoundException {
		synchronized(transfers) {
			if(id == null) {
				if(transfers.isEmpty()) throw new TransferNotFoundException("Transfer with empty id not found for call " + this.sipId);
				
				return transfers.get(0); //if id is null, we return the first transfer that was registered
			}
			
			long lid = Long.parseLong(id);
			for(Transfer transfer : transfers) {
				if(transfer.getId() == lid) {
					return transfer;
				}
			}
			throw new TransferNotFoundException("Transfer with id " + id + " not found for call " + this.sipId);
		}
	}

	public void finishTransfer(Transfer transfer) {
		transfers.remove(transfer);
	}

	@Override
	public String toString() {
		return "JainSipCall [id=" + id + 
				", A=" + params.getFrom() +
				", B=" + params.getTo() +
				", type=" + params.getCallType() + 	
				"]";
	}
	
	
}
