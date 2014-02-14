package com.rcslabs.rcl;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.conferencing.IConferenceService;
import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.IService;
import com.rcslabs.rcl.core.IAddressUri;
import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.core.entity.IConnectionParams;
import com.rcslabs.rcl.core.entity.IModifiableConnectionParams;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.core.event.IConnectionEvent.Type;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.event.ConnectionEvent;
import com.rcslabs.rcl.messaging.IMessagingService;
import com.rcslabs.rcl.presence.IPresenceService;
import com.rcslabs.rcl.presence.entity.PresenceStatus;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.entity.CallParams;

public class JainSipConnection
extends AbstractListenable<IConnectionListener, ConnectionEvent>
implements IConnection, IConnectionParams {
	private static Logger log = LoggerFactory.getLogger(JainSipConnection.class);
	
	private final JainSipRclFactory factory;
	private final JainSipCallManager callManager;
	private IModifiableConnectionParams params;
	private ConcurrentMap<Object, Object> appData = new ConcurrentHashMap<Object, Object>();
	private boolean connected = false;
	private boolean disconnecting = false;
	private final String id = UUID.randomUUID().toString();
	private JainSipTelephonyService telephonyService;
	private JainSipPresenceService presenceService;
	private JainSipMessagingService messagingService;
	private ScheduledFuture<?> reRegisterFuture;
	private ScheduledFuture<?> expireFuture;
	private AtomicLong cSeqs = new AtomicLong(1);
	private boolean presenceEnabled = false;

	public JainSipConnection(JainSipRclFactory factory, JainSipCallManager callManager) {
		this.factory = factory;
		this.callManager = callManager;
		this.telephonyService = new JainSipTelephonyService(this, callManager);
		this.presenceService = new JainSipPresenceService(this, callManager);
		this.messagingService = new JainSipMessagingService(this, callManager);
	}

	
	
	public void open(IConnectionParams params) {
		if(this.params != null) throw new RclException("Connection was already used. Consider creating a new connection object instead.");
		if(params == null) throw new IllegalArgumentException("Params cannot be null");
		if(params.getPhoneNumber() == null) throw new IllegalArgumentException("Mandatory parameter phoneNumber is absent.");
		
		try {
			this.params = params.clone();
			
			//parse domain name in phoneNumber if present
			if(this.params.getPhoneNumber().contains("@")) {
				String oldPhoneNumber = this.params.getPhoneNumber();
				int domainInd = oldPhoneNumber.indexOf("@");
				this.params.setPhoneNumber(
						oldPhoneNumber.substring(0, domainInd)
				);
				String domainName = oldPhoneNumber.substring(domainInd + 1);
				if(!domainName.equals(callManager.getGlobalParams().getSipServerAddress())) {
					this.params.setDomainName(domainName);
				}
			}
			
			this.presenceEnabled = params.isPresenceEnabled();
			if(!params.isSkipAuthentication()) {
				log.debug("Registering...");
				callManager.register(this);				
			}
			else {
				log.debug("Won't register, skipAuthentication is true");
				setConnected(true);
				fireEvent(new ConnectionEvent(Type.CONNECTED));
			}
			
			log.debug("All done.");
		}
		catch(Exception e) {
			throw new RclException("Register failed for user " + params.getPhoneNumber(), e);
		}
	}
	
	public void close() {
		if (disconnecting) {
			return;
		} else {
			disconnecting = true;
		}
		
		if(params == null) {
			factory.release(this);
			return;
		}
		
		try {
			stopFutures();
			
			if(!isConnected()) {
				return;
			}
			
			if(params.isPresenceEnabled()) {
				log.debug("Setting OFFLINE status...");
				presenceService.setStatus(PresenceStatus.OFFLINE);
				
				log.debug("Unsibscribing from events...");
				presenceService.unsubscribe();
			}
			
			log.debug("Stopping all calls...");
			stopAllCalls();
			
			setConnected(false);
			
			if(!params.isSkipAuthentication()) {
				// https://redmine.rcslabs.ru/issues/851
				if(params.isPresenceEnabled()) {
					try {
						callManager.shutdownPresence(this);
					}
					catch(Exception e) {
						log.error("Failed to shutdown presence for user" + params.getUserName(), e);
					}
				}
				
				log.debug("Unregistering...");
				callManager.unregister(this);
			}
				
			fireEvent(new ConnectionEvent(Type.CONNECTION_BROKEN));
			
			log.debug("All done.");
		} catch (Exception e) {
			callManager.removeConnectionFromPool(this);
			log.error("Error during close for user " + params.getPhoneNumber() + ", connection was closed forcibly.", e);
		} finally {
			factory.release(this);
		}
	}
	
	public void kick(JainSipConnection by) {
		if (disconnecting) {
			return;
		} else {
			disconnecting = true;
		}

		if(params == null) {
			factory.release(this);
			return;
		}

		stopFutures();

		presenceEnabled = false;
		
		log.debug("Kicked, stopping all calls...");
		stopAllCalls();
		
		setConnected(false);
		fireEvent(new ConnectionEvent(Type.CONNECTION_BROKEN));
	}
	
	private void stopFutures() {
		if (reRegisterFuture!=null) {
			reRegisterFuture.cancel(true);
			reRegisterFuture = null;
		}
		if (expireFuture!=null) {
			expireFuture.cancel(true);
			expireFuture = null;
		}
	}
	
	private void stopAllCalls() {
		for(ICall call : telephonyService.getCalls()) {
			try {
				call.finish();
			}
			catch(Exception e) {
				log.error("Failed to finish call " + call, e);
			}
		}
	}

	public JainSipCall newCall() {
		if(!connected) throw new RclException("Cannot create new call: not connected.");
		return factory.newCall(this);
	}
	
	public JainSipCall newCall(CallParams callParams) {
		return factory.newCall(this, callParams);
	}

	public IConnectionParams getParams() {
		return this;
	}

	public void fireEvent(ConnectionEvent connectionEvent) {
		connectionEvent.setConnection(this);
		super.fireEvent(connectionEvent);
	}
	
	public void fireErrorEvent(String errorText, Exception cause) {
		log.error("Firing error: " + errorText, cause);
		ConnectionEvent event = new ConnectionEvent(Type.CONNECTION_ERROR);
		ErrorInfo errInfo = new ErrorInfo();
		errInfo.setErrorText(errorText);
		errInfo.setCause(new RclException(cause));
		event.setErrorInfo(errInfo);
		fireEvent(event);
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

	private void setConnected(boolean connected) {
		this.connected = connected;
	}

	private boolean isConnected() {
		return connected;
	}

	@Override
	public String getId() {
		return id;
	}

	public Collection<JainSipCall> getCalls() {
		return callManager.getCallsForConnection(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IService> T getService(Class<T> cls) throws ServiceNotEnabledException {
		if(ITelephonyService.class.isAssignableFrom(cls)) {
			return (T)telephonyService;
		}
		else if(IPresenceService.class.isAssignableFrom(cls)) {
			return (T)presenceService;
		}
		else if(IMessagingService.class.isAssignableFrom(cls)) {
			return (T)messagingService;
		}
		
		throw new ServiceNotEnabledException("Service " + cls.getName() + " not enabled.");
	}
	
	@Override
	public <T extends IService> boolean isServiceEnabled(Class<T> cls) {
		if(ITelephonyService.class.isAssignableFrom(cls)) {
			return true;
		}
		else if(IPresenceService.class.isAssignableFrom(cls)) {
			return true;
		}
		else if(IConferenceService.class.isAssignableFrom(cls)) {
			return true;
		}
		else if(IMessagingService.class.isAssignableFrom(cls)) {
			return true;
		}
		
		return false;
	}
	
	public JainSipTelephonyService getTelephonyService() {
		return telephonyService;
	}
	
	public JainSipPresenceService getPresenceService() {
		return presenceService;
	}

	public void onSuccessfulDeregistration() {
		this.setConnected(false);
		callManager.removeConnectionFromPool(this);
	}
	
	/**
	 * Informs Connection about successful registration
	 * @param expires expires timeout in seconds from SIP server 
	 */
	public void onSuccessfulRegistration(long expires) {
		if (disconnecting) {
			return;
		}
		if(!isConnected()) {
			setConnected(true);
			callManager.addConnectionToPool(this);
			fireEvent(new ConnectionEvent(IConnectionEvent.Type.CONNECTED));
		}
		this.reRegisterFuture = callManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					log.debug("Re-register connection " + JainSipConnection.this.getFullId()+" - STARTING...");
					callManager.register(JainSipConnection.this);
				} catch (Exception e) {
					log.error("Re-register connection " + JainSipConnection.this.getFullId()+" - FAILED ("+e.getMessage()+")", e);
					JainSipConnection.this.fireErrorEvent("Failed to re-connect", e);
				}
			}
		}, expires - factory.getGlobalParams().getRegisterBeforeExpirationTime(), TimeUnit.SECONDS);
		ScheduledFuture<?> expire = expireFuture;
		if (expire!=null) {
			expire.cancel(true);
		}
		this.expireFuture = callManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					log.debug("Connection " + JainSipConnection.this.getFullId()+" is expired");
					JainSipConnection.this.setConnected(false);
					callManager.removeConnectionFromPool(JainSipConnection.this);
					ScheduledFuture<?> expire = expireFuture;
					if (expire!=null) {
						expire.cancel(true);
					}
					expireFuture = null;
					fireEvent(new ConnectionEvent(Type.CONNECTION_BROKEN));
				} catch (Exception e) {
					log.error("Connection " + JainSipConnection.this.getFullId() + ": failed to expire connection", e);
					JainSipConnection.this.fireErrorEvent("Failed to expire connection", e);
				}
			}
		}, expires, TimeUnit.SECONDS);
	}
	
	public long getNextCSeq() {
		return cSeqs.getAndIncrement();
	}

	@Override
	public String getUserName() {
		return params.getUserName();
	}

	@Override
	public String getPhoneNumber() {
		return params.getPhoneNumber();
	}

	@Override
	public String getPassword() {
		return params.getPassword();
	}

	@Override
	public boolean isPresenceEnabled() {
		return presenceEnabled;
	}

	@Override
	public boolean isSkipAuthentication() {
		return params.isSkipAuthentication();
	}

	@Override
	public String getServerAddress() {
		return params.getServerAddress();
	}

	@Override
	public String getProxyAddress() {
		return params.getProxyAddress();
	}
	
	public IModifiableConnectionParams clone() {
		return params.clone();
	}

	@Override
	public String getDomainName() {
		return params.getDomainName();
	}
	
	public String getFullId() {
		return getId()+" ("+getPhoneNumber()+"@"+getDomainName()+")";
	}

	public IAddressUri getUri() {
		SipAddressUri uri = new SipAddressUri();
		uri.setDomainName(this.getDomainName()==null?this.callManager.getGlobalParams().getSipServerAddress():this.getDomainName());
		uri.setProtocol("sip");
		uri.setPhoneNumber(params.getPhoneNumber());
		uri.setUserName(this.getUserName());
		return uri;
	}



	public final void setParams(IModifiableConnectionParams params) {
		this.params = params;
	}
	
}
