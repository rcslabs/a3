package ru.rcslabs.webcall.server;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import ru.rcslabs.webcall.server.api.ICallService;
import ru.rcslabs.webcall.server.api.IClientConnection;
import ru.rcslabs.webcall.server.event.adapter.CallEventListenerAdapter;
import ru.rcslabs.webcall.server.event.adapter.ConferenceEventListenerAdapter;
import ru.rcslabs.webcall.server.event.adapter.ConnectionEventListenerAdapter;

import com.rcslabs.rcl.conferencing.IConference;
import com.rcslabs.rcl.conferencing.IConferenceService;
import com.rcslabs.rcl.conferencing.entity.ConferenceMember;
import com.rcslabs.rcl.conferencing.event.IConferenceEvent;
import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.entity.ConnectionParams;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.entity.CallParams;
import com.rcslabs.rcl.telephony.entity.CallType;
import com.rcslabs.rcl.telephony.entity.RejectReason;
import com.rcslabs.rcl.translation.DefaultDynamicHeader.IRuntimeCallDataParams;
import com.rcslabs.util.CollectionUtils;

public abstract class AbstractCallService 
extends WebcallApplicationService 
implements ICallService {
	protected Logger log;
	
	private List<ICallListener> customCallListeners;

	/**
	 * Creates a {@link IClientConnection} object to perform callback actions
	 * @return a {@link IClientConnection} object to perform callback actions
	 */
	protected abstract IClientConnection createCallClient();
	
	/*
	 * ----------------- ICallService implementation ------------------
	 */
	@Override
	public String open(String phone, String password, Object... params) throws ServiceNotEnabledException {
		IConnection conn = getApplication().getRclFactory().newConnection();
		IClientConnection client = createCallClient();
		conn.setApplicationData(AppDataConstants.WEB_CONNECTION, client);
		conn.addListener(new ConnectionEventListenerAdapter());
		conn.getService(ITelephonyService.class).addListener(
			new CallEventListenerAdapter(getApplication().getDispatcher())
		);
		try {
			conn.getService(IConferenceService.class).addListener(
					ConferenceEventListenerAdapter.DEFAULT_INSTANCE
			);
		}
		catch(ServiceNotEnabledException e) {
			log.warn("Not subscribing to Conference Service events: {}", e.getMessage());
		}
		getApplication().getDispatcher().fireConnectionCreated(conn);

		ConnectionParams cnParams = new ConnectionParams();

		// params.setPhoneNumber(phone);
		AddressUri contact = new AddressUri(phone);
		cnParams.setPhoneNumber(contact.getAddress());
		cnParams.setUserName(contact.getDisplayName());

		cnParams.setPassword(password);
		conn.open(cnParams);
		
		return conn.getId();
	}

	@Override
	public void close(String uid) {
		IConnection conn = getConnection(uid);
		IClientConnection clientConn = 
				(IClientConnection)conn.getApplicationData(AppDataConstants.WEB_CONNECTION);
		conn.close();
		// TODO: is it necessary to fire ConnectionDestroyed event right here ???  
		getApplication().getDispatcher().fireConnectionDestroyed(conn, uid, clientConn);
	}
	
	private ICall getCall(String connectionUid, String callUid) {
		try {
			ICall ret = getConnection(connectionUid)
					.getService(ITelephonyService.class)
					.findCall(callUid);
			if(ret == null) throw new RclException("No call with id " + callUid);
			
			return ret;
		} catch (ServiceNotEnabledException e) {
			throw new RclException(e);
		}
	}
	
	private IConference getConference(String connectionUid, String confUid) {
		try {
			IConference ret = getConnection(connectionUid)
					.getService(IConferenceService.class)
					.findConference(confUid);
			if(ret == null) throw new RclException("No conference with id " + confUid);
			
			return ret;
		} catch (ServiceNotEnabledException e) {
			throw new RclException(e);
		}
	}
	
	protected IClientConnection getCallClient(IConnection connection) {
		return (IClientConnection)connection.getApplicationData(AppDataConstants.WEB_CONNECTION);
	}
	
	protected IClientConnection getCallClient(ICall call) {
		return getCallClient(call.getConnection());
	}
	
	protected IClientConnection getCallClient(IConferenceEvent confEvent) {
		return getCallClient(confEvent.getConference().getCall());
	}

	@Override
	public String call(String uid, String destination, String av_params) throws ServiceNotEnabledException {
		final IConnection conn = getConnection(uid);
		ICall call = conn.getService(ITelephonyService.class).newCall();
		//call.addListener(new CallEventListenerAdapter(getApplication().getDispatcher()));
		getApplication().getDispatcher().fireCallCreated(call); //listener will be added here
		CallParams callParams = new CallParams();
		
		if (destination!=null) {
			AddressUri contact = new AddressUri(destination);
			callParams.setToPhoneNumber(contact.getAddress());
			callParams.setToUserName(contact.getDisplayName());
		}

		if (StringUtils.isBlank(av_params)) {
			callParams.setCallType(CallType.AUDIO);
		}
		else {
			callParams.setCallType(CallType.fromString(av_params));
		}
		
		callParams.setCustomParams(new IRuntimeCallDataParams() {
			@Override
			public Object getValue(String key) {
				if("call-start-time".equals(key)) {
					return new Date();
				}
				else if("source-ip".equals(key)) {
					return getCallClient(conn).getClientIpAddress();
				}
				else {
					@SuppressWarnings("unchecked")
					Map<String, Object> rcdp = (Map<String, Object>)conn
							.getApplicationData(AppDataConstants.RUNTIME_CALL_DATA_PARAMS);
					if(rcdp != null) {
						return rcdp.get(key);
					}
				}
				
				return null;
			}
			@Override
			public IRuntimeCallDataParams clone() {
				try {
					return (IRuntimeCallDataParams)super.clone();
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		call.start(callParams);
		
		return call.getId();
	}

	@Override
	public void accept(String uid, String callId, String av_params) {
		ICall call = getCall(uid, callId);

		call.accept(
				StringUtils.isNotBlank(av_params) ? CallType.fromString(av_params) : CallType.AUDIO
		);
	}

	@Override
	public void decline(String uid, String callId) {
		reject(uid, callId, RejectReason.DECLINE);
	}

	@Override
	public void busy(String uid, String callId) {
		reject(uid, callId, RejectReason.BUSY);
	}

	@Override
	public void unavailable(String uid, String callId) {
		reject(uid, callId, RejectReason.UNAVAILABLE);
	}

	private void reject(String uid, String callId, RejectReason reason) {
		ICall call = getCall(uid, callId);
		call.reject(reason);
		// TODO: is it necessary to fire CallDestroyed event right here ???  
		getApplication().getDispatcher().fireCallDestroyed(call);
	}

	@Override
	public void dtmf(String uid, String callId, String dtmf) {
		ICall call = getCall(uid, callId);
		call.dtmf(dtmf);
	}

	@Override
	public void hangup(String uid, String callId) {
		ICall call = getCall(uid, callId);
		call.finish();
	}

	@Override
	public void transfer(String uid, String callId, String destination) {
		ICall call = getCall(uid, callId);

		CallParams toParams = new CallParams();
		toParams.setToPhoneNumber(destination);
		call.transfer(toParams);
	}
	
	@Override
	public String startConference(String uid) throws ServiceNotEnabledException {
		IConnection connection = getConnection(uid);
		IConferenceService conferenceService = connection.getService(IConferenceService.class);
		IConference conference = conferenceService.newConference();
		conference.addListener(ConferenceEventListenerAdapter.DEFAULT_INSTANCE);
		conference.start();
		return conference.getId();
	}
	
	public void inviteMember(String uid, String confId, String phoneNumber) {
		IConference conference = getConference(uid, confId);
		ConferenceMember member = new ConferenceMember();
		member.setPhoneNumber(phoneNumber);
		conference.inviteMember(member);
	}
	
	public void kickMember(String uid, String confId, String phoneNumber) {
		IConference conference = getConference(uid, confId);
		ConferenceMember member = new ConferenceMember();
		member.setPhoneNumber(phoneNumber);
		conference.kickMember(member);
	}
	
	public void acceptConferenceInvitation(String uid, String confId) {
		getConference(uid, confId).acceptInvitation();
	}
	
	public void rejectConferenceInvitation(String uid, String confId) {
		getConference(uid, confId).rejectInvitation();
	}
	
	public void finishConference(String uid, String confId) {
		getConference(uid, confId).finish();
	}
	
	@Override
	public void onClientDisconnected(final IClientConnection client) {
		getApplication().getRclFactory().forEachConnection(
				new CollectionUtils.Criteria<IConnection>() {
					@Override
					public boolean satisfies(IConnection connection) {
						IClientConnection c = 
								(IClientConnection)connection
								.getApplicationData(AppDataConstants.WEB_CONNECTION);
						
						return client.equals(c);
					}
				},
				new CollectionUtils.Operation<IConnection>() {
					@Override
					public boolean run(IConnection connection) {
						connection.close();
						return true;
					}
				}
		);
	}
	
	@Override
	public void onCallCreated(ICall call) {
		call.addListener(new CallEventListenerAdapter(getApplication().getDispatcher()));
		if(customCallListeners != null) {
			for(ICallListener listener : customCallListeners) {
				call.addListener(listener);
			}
		}
	}

	public void setCustomCallListeners(List<ICallListener> customCallListeners) {
		this.customCallListeners = customCallListeners;
	}

}
