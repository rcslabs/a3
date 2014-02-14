package com.rcslabs.rcl;

import gov.nist.javax.sip.header.SIPHeaderNames;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.AddressFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.message.SipAckRequestObject;
import com.rcslabs.rcl.message.SipByeRequestObject;
import com.rcslabs.rcl.message.SipCancelRequestObject;
import com.rcslabs.rcl.message.SipInviteRequestObject;
import com.rcslabs.rcl.message.SipMessageRequestObject;
import com.rcslabs.rcl.message.SipNotifyRequestObject;
import com.rcslabs.rcl.message.SipPublishRequestObject;
import com.rcslabs.rcl.message.SipReferRequestObject;
import com.rcslabs.rcl.message.SipRegisterRequestObject;
import com.rcslabs.rcl.message.SipRequestObject;
import com.rcslabs.rcl.message.SipResponseObject;
import com.rcslabs.rcl.message.SipSubscribeRequestObject;
import com.rcslabs.rcl.presence.entity.IContactPresence;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.util.CollectionUtils;
import com.rcslabs.util.CollectionUtils.Criteria;
import com.rcslabs.util.CollectionUtils.Operation;
import com.rcslabs.util.IpAddressUtils;

/**
 * Manages SIP Server connection and transfers SIP requests-responces to
 * SIP Message Object hierarchy (com.rcslabs.rcl.impl.message).
 *
 */
class JainSipCallManager implements SipListener, ICallManager {
	private static final long UNREGISTER_WAIT_TIMEOUT = 10000;
	
	private static Logger log = LoggerFactory.getLogger(JainSipCallManager.class);
	private static AtomicInteger instanceCounter = new AtomicInteger();
	
	//private static final long TIMEOUT_JOIN_REREGISTERTHREAD = 1000; // 1000 msec
	//private static final long TIMEPERIOD_REREGISTERTHREAD = 1000; // 1000 msec
	
	private int instanceId = instanceCounter.incrementAndGet();

	private SipStack stack;
	private SipProvider provider;

	private AddressFactory addressFactory;
	private HeaderFactory headerFactory;
	private MessageFactory messageFactory;

	private final JainSipGlobalParams globalParams;
	
	private SdpHelper sdpHelper = new SdpHelper();
	
	//private Map<String, JainSipConnection> connections = new ConcurrentHashMap<String, JainSipConnection>();
	private List<JainSipConnection> allconnections = new CopyOnWriteArrayList<JainSipConnection>();
	private Map<String, JainSipConnection> phone2connection = new ConcurrentHashMap<String, JainSipConnection>();
	
	private Map<String, JainSipCall> calls = new ConcurrentHashMap<String, JainSipCall>();
	
	private PresenceHelper presenceHelper = new PresenceHelper();
	
	private VersionHelper versionHelper = new VersionHelper();
	
	private ConferenceHelper conferenceHelper = new ConferenceHelper();

	//private final IRclMediaFactory mediaFactory;
	
	private ScheduledExecutorService scheduler;
	
	//Audits the underlying SIP stack, trying to eliminate memory leaks. 
	private class SipStackAuditTask extends TimerTask {
		private static final long AUDIT_INTERVAL_MS = 2 * 60 * 1000;
		private static final long DIALOG_TIMEOUT_MS = 60 * 60 * 1000;
		private static final long TRANSACTION_TIMEOUT_MS = 60 * 1000;
		
        public final void run() {
        	log.info("Starting SIP stack audit...");
        	
            Set<String> activeCallIDs = calls.keySet();
            String auditReport = ((SIPTransactionStack) stack).auditStack(
            		activeCallIDs,
                    DIALOG_TIMEOUT_MS, 
                    TRANSACTION_TIMEOUT_MS);
            
            if(auditReport != null) {
                log.warn("SIP stack audit ALERT!!!\n" + auditReport);
            } else {
                log.info("SIP stack audit finished. No leaks detected.");
            }
        }
    }
	
	public JainSipCallManager(JainSipGlobalParams params) {
		//this.mediaFactory = mediaFactory;
		this.globalParams = (JainSipGlobalParams)params.clone();
		this.sdpHelper.setUse0port4acceptVideoAsAudio(this.globalParams.isUse0port4acceptVideoAsAudio());
		if (this.globalParams.getH264PayloadType()!=null) {
			this.sdpHelper.setH264PayloadType(this.globalParams.getH264PayloadType());
		}
	}

	public void start() {
		if(isStarted()) throw new RclException("CallManager is already started.");
		
		log.info("SIP stack starting...");
		/* ZDS HACK multiple SipFactories
		SipFactory sipFactory = SipFactory.getInstance();
		*/
		Constructor<?> c[] = SipFactory.class.getDeclaredConstructors();
		c[0].setAccessible(true);
		SipFactory sipFactory;
		try {
			sipFactory = (SipFactory)c[0].newInstance((Object[])null);
		} catch (IllegalArgumentException e1) {
			throw new RclException(e1);
		} catch (InstantiationException e1) {
			throw new RclException(e1);
		} catch (IllegalAccessException e1) {
			throw new RclException(e1);
		} catch (InvocationTargetException e1) {
			throw new RclException(e1);
		}
		/* ZDS END OF HACK */
		//TODO: to fix this right, we need to use jain-sip-ri from jboss.org (http://ci.jboss.org/hudson/view/All/job/jain-sip/lastSuccessfulBuild/artifact/)
		
		sipFactory.setPathName("gov.nist"); // This is the package prefix for implementation class !!!
		Properties props = new Properties();
		
		//set local IP address for SIP
		String localIpAddress = globalParams.getLocalIpAddress();
		if(StringUtils.isBlank(localIpAddress)) {
			localIpAddress = IpAddressUtils.getLocalIpAddress();
			if(localIpAddress == null) 
				throw new RclException("Local IP address not specified and could not be automatically found.");
			log.info("Local IP address is: {}", localIpAddress);
			globalParams.setLocalIpAddress(localIpAddress);
		}
		props.setProperty("javax.sip.STACK_NAME", "com.rcslabs.sipstack" + instanceId);
		props.setProperty("javax.sip.IP_ADDRESS", localIpAddress);
		//props.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off"); - default is off
		
		//set IP address for media
		String mediaIpAddress = globalParams.getMediaIpAddress();
		if(StringUtils.isBlank(mediaIpAddress)) {
			mediaIpAddress = localIpAddress;
		}
		sdpHelper.setIpAddress(mediaIpAddress);
		
		if(globalParams.isFullLogging()) {
			props.setProperty("gov.nist.javax.sip.STACK_LOGGER", JainSipLogger.class.getName());
		} else {
			props.setProperty("gov.nist.javax.sip.STACK_LOGGER", JainSipEmptyLogger.class.getName());
		}
		props.setProperty("gov.nist.javax.sip.AGGRESSIVE_CLEANUP", "true");

		if (globalParams.getSipProxyAddress()==null) {
			globalParams.setSipProxyAddress(globalParams.getSipServerAddress());
			globalParams.setSipProxyPort(globalParams.getSipServerPort());
		}
		
		if(StringUtils.isNotBlank(globalParams.getSipProxyAddress())) {
			props.setProperty(
					"javax.sip.OUTBOUND_PROXY",
					String.format(
							"%s:%d/UDP", 
							globalParams.getSipProxyAddress(), 
							(globalParams.getSipProxyPort() != 0 ? globalParams.getSipProxyPort() : 5060)
					)
			);
		}
		
		//initialize the SIP stack
		try {			
			stack = sipFactory.createSipStack(props);

			try {
				stack.start();

				provider = stack.createSipProvider(
						stack.createListeningPoint(
								localIpAddress, 
								globalParams.getLocalPort(), 
								ListeningPoint.UDP)
				);

				addressFactory = sipFactory.createAddressFactory();
				headerFactory = sipFactory.createHeaderFactory();
				messageFactory = sipFactory.createMessageFactory();

				provider.setAutomaticDialogSupportEnabled(true);
				provider.addSipListener(this);
				
//				reRegisterAgentThread = new Thread(reRegisterAgent);
//				reRegisterAgentThread.start();
			}
			catch(Exception e) {
				try {
					stack.stop();
				}
				catch(Exception e2) {
					//ignore
				}
				throw e;
			}
		}
		catch(Exception e) {
			throw new RclException("Failed to start " + getClass().getSimpleName(), e);
		}
		
		scheduler = Executors.newScheduledThreadPool(1);
		
		if(globalParams.isEnableSipAudit()) {
			log.info("Scheduling SIP audit...");
			scheduler.scheduleAtFixedRate(
					new SipStackAuditTask(), 
					SipStackAuditTask.AUDIT_INTERVAL_MS, 
					SipStackAuditTask.AUDIT_INTERVAL_MS, 
					TimeUnit.MILLISECONDS
			);
		}
		
		log.info("SIP stack started.");
	}

	public void stop() {
		if(!isStarted()) return; // CallManager is already stopped.
		
		scheduler.shutdownNow();
		log.info("Waiting for all connections to close...");
		waitForConnectionsClose();
		log.info("SIP stack stopping...");
		provider.removeSipListener(this);

		try {
			stack.deleteSipProvider(provider);
		} catch (ObjectInUseException e) {
			log.warn("Error during deleting sipprovider from sipstack");
		}

		provider = null;
		stack.stop();
		stack = null;

		log.info("SIP stack stopped.");
	}
	
	public boolean isStarted() {
		return stack != null;
	}

	private void waitForConnectionsClose() {
		try {
			int waitPeriod = (int)(UNREGISTER_WAIT_TIMEOUT * (1 + 0.1f * allconnections.size()));
			while(waitPeriod > 0 && allconnections.size() > 0) {
				if(log.isDebugEnabled()) { //to avoid excessive calls to connections.size()
					log.debug("waitPeriod = {}, connections size = {}", waitPeriod, allconnections.size());
				}
				Thread.sleep(1000);
				waitPeriod -= 1000;
			}
		}
		catch(InterruptedException e) {
			log.warn("Interrupted");
		}
	}

	public void register(JainSipConnection connection) throws Exception {
		new SipRegisterRequestObject(this, connection, globalParams.getExpires()).send();
	}

	public void unregister(JainSipConnection connection) throws Exception {
		new SipRegisterRequestObject(this, connection, 0).send();
	}

	public void invite(JainSipCall call) throws Exception {
		new SipInviteRequestObject(this, call).send();
	}
	
	public void bye(JainSipCall call) throws Exception {
		new SipByeRequestObject(this, call).send();
	}
	
	public void cancel(JainSipCall call) throws Exception {
		new SipCancelRequestObject(this, call).send();
	}
	
	private void sendResponse(RequestEvent event, int responseType) {
		try {
			Response response = messageFactory.createResponse(responseType, event.getRequest());
			log.debug("sendResponse: {}", response);
			
			ServerTransaction serverTransaction = event.getServerTransaction();
			if(serverTransaction != null) {
				serverTransaction.sendResponse(response);
			}
			else {
				provider.sendResponse(response);
			}
		}
		catch(Exception e) {
			log.error("Failed to respond", e);
		}
	}

	public void processDialogTerminated(DialogTerminatedEvent event) {
		log.debug("processDialogTerminated: {}", event);
		Dialog dialog = event.getDialog();
		if(dialog != null) {
			dialog.delete();
		}
	}

	public void processIOException(IOExceptionEvent ioexceptionevent) {
		log.debug("processIOException: {}", ioexceptionevent);
	}

	//converts incoming SIP requests into SipMessageObject hierarchy
	public void processRequest(RequestEvent event) {
		log.debug("processRequest: {}", event.getRequest());
		String method = event.getRequest().getMethod();
		log.debug("method: {}", method);
		
		try {
			JainSipCall call = 
				calls.get(((CallIdHeader)event.getRequest().getHeader(SIPHeaderNames.CALL_ID)).getCallId());
			
			if(Request.BYE.equals(method)) {
				new SipByeRequestObject(this, call).process(event);
			}
			else if(Request.INVITE.equals(method)) {
				new SipInviteRequestObject(this).process(event);
			}
			else if(Request.ACK.equals(method)) {
				new SipAckRequestObject(this).process(event);
			}
			else if(Request.NOTIFY.equals(method)) {
				new SipNotifyRequestObject(this).process(event);
			}
			else if(Request.CANCEL.equals(method)) {
				new SipCancelRequestObject(this, call).process(event);
			}
			else if(Request.REFER.equals(method)) {
				new SipReferRequestObject(this, call, null).process(event);
			}
			else if(Request.MESSAGE.equals(method)) {
				new SipMessageRequestObject(this).process(event);
			}
			else {
				try {
					sendResponse(event, Response.METHOD_NOT_ALLOWED);
				} catch (Exception e) {
					log.error("Failed to send response", e);
				}
			}
		}
		catch(Exception e) {
			log.error("Exception in processRequest", e);
		}
	}

	//converts incoming SIP responses into SipMessageObject hierarchy
	public void processResponse(ResponseEvent event) {
		log.debug("processResponse: {}", event.getResponse());
		
		try {
			ClientTransaction transaction = event.getClientTransaction();
			if (transaction==null) {
				log.warn("Unknown transaction for event {}", event);
				return;				
			}
			SipRequestObject requestObject = (SipRequestObject)transaction.getApplicationData();
			if(requestObject == null) {
				log.warn("Null request object.");
				return;
			}
			SipResponseObject responseObject = requestObject.createResponseObject();
			
			if(responseObject != null) {
				responseObject.doResponse(event);
			}
			else {
				log.error("Null response object for request object {}", requestObject);
			}
		}
		catch(Exception e) {
			log.error("Exception in processResponse", e);
		}
	}

	public void processTimeout(TimeoutEvent event) {
		log.debug("processTimeout: " + event);
		
		try {
			ClientTransaction transaction = event.getClientTransaction();
			if (transaction==null) {
				log.warn("Unknown transaction for event {}", event);
				return;				
			}
			SipRequestObject requestObject = (SipRequestObject)transaction.getApplicationData();
			if(requestObject == null) {
				log.warn("Null request object.");
				return;
			}
			requestObject.doTimeout(event);
		}
		catch(Exception e) {
			log.error("Exception in processTimeout", e);
		}
	}

	public void processTransactionTerminated(TransactionTerminatedEvent event) {
		log.debug("processTransactionTerminated: " + event);
		ClientTransaction clientTransaction = event.getClientTransaction();
		if(clientTransaction != null) {
			clientTransaction.setApplicationData(null);
		}
		
		ServerTransaction serverTransaction = event.getServerTransaction();
		if(serverTransaction != null) {
			serverTransaction.setApplicationData(null);
		}
	}

	public Collection<JainSipCall> getCallsForConnection(final JainSipConnection connection) {
		return CollectionUtils.subCollection(calls.values(), new CollectionUtils.Criteria<JainSipCall>() {
			@Override
			public boolean satisfies(JainSipCall call) {
				return call.getConnection() == connection;
			}
		});
	}

	public void subscribe(String phoneNumber, JainSipConnection connection, String eventType, Dialog dialog) throws Exception {
		new SipSubscribeRequestObject(
				this, 
				connection, 
				phoneNumber, 
				/* "presence" */eventType, 
				dialog,
				globalParams.getSubscribeExpires())
		.send();
	}

	public void unsubscribe(String phoneNumber, JainSipConnection connection) throws Exception {
		new SipSubscribeRequestObject(
				this, 
				connection, 
				phoneNumber, 
				"presence", 
				null,
				0)
		.send();
	}
	
	public void initPresence(JainSipConnection connection) throws Exception {
		new SipSubscribeRequestObject(
				this, 
				connection, 
				connection.getParams().getPhoneNumber(), 
				"presence.winfo", 
				null,
				globalParams.getSubscribeExpires())
		.send();
	}
	
	public void shutdownPresence(JainSipConnection connection) throws Exception {
		new SipSubscribeRequestObject(
				this, 
				connection, 
				connection.getParams().getPhoneNumber(), 
				"presence.winfo", 
				null,
				0)
		.send();
	}
	
	public void publish(JainSipConnection connection, IContactPresence presence) throws Exception {
		new SipPublishRequestObject(this, connection, presence).send();
	}
	
	public void transfer(JainSipCall call, ICallParams toParams) throws Exception {
		new SipReferRequestObject(this, call, toParams).send();
	}

	@Override
	public void addConnectionToPool(JainSipConnection connection) {
		String userPhone = connection.getPhoneNumber();
		if (globalParams.isEnableExclusiveRegister()) {
			//check if we already had a connection
			JainSipConnection previousConnection = phone2connection.get(userPhone);
			if(previousConnection != null) {
				log.info("Connection {} will kick previous connection {}", connection.getId(), previousConnection.getId());
				previousConnection.kick(connection);
				removeConnectionFromPool(previousConnection);
			}
			phone2connection.put(userPhone, connection);
		}
		allconnections.add(connection);
//		if(connections.containsKey(userPhone)) throw new RclException("Internal error: Connection with phone " + userPhone + " already present in pool.");
		log.debug("Connection added to pool. Phone: {}", userPhone);
	}
	
	@Override
	public void removeConnectionFromPool(JainSipConnection connection) {
		allconnections.remove(connection);
		if (globalParams.isEnableExclusiveRegister()) {
			phone2connection.remove(connection.getPhoneNumber());
		}
		log.debug("Connection removed from pool. Phone: {}", connection.getPhoneNumber());
	}
	
	public IConnection getConnectionById(final String id) {
		return CollectionUtils.findSingle(
				allconnections, 
				new CollectionUtils.Criteria<JainSipConnection>() {
					@Override
					public boolean satisfies(JainSipConnection connection) {
						return connection.getId().equals(id);
					}
				}
		);
	}
	
	@Override
	public void forEachConnectionInPool(Operation<JainSipConnection> operation) {
		CollectionUtils.forEach(allconnections, operation);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void forEachConnectionInPool(
			Criteria<IConnection> criteria, 
			Operation<IConnection> operation) 
	{
		CollectionUtils.forEach((Collection)allconnections, criteria, operation);
	}

	@Override
	public AddressFactory getAddressFactory() {
		return addressFactory;
	}

	@Override
	public JainSipGlobalParams getGlobalParams() {
		return globalParams;
	}

	@Override
	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	@Override
	public MessageFactory getMessageFactory() {
		return messageFactory;
	}

	@Override
	public SipProvider getProvider() {
		return provider;
	}

	@Override
	public VersionHelper getVersionHelper() {
		return versionHelper;
	}

	@Override
	public void addCallToPool(String sipId, JainSipCall call) {
		calls.put(sipId, call);
	}
	
	@Override
	public void removeCallFromPool(String sipId) {
		calls.remove(sipId);
	}
	
	@Override
	public JainSipCall getCallFromPool(String sipId) {
		return calls.get(sipId);
	}
	
	public ICall getCallFromPoolById(final String id) {
		return CollectionUtils.findSingle(
				calls.values(), 
				new CollectionUtils.Criteria<JainSipCall>() {
					@Override
					public boolean satisfies(JainSipCall call) {
						return call.getId().equals(id);
					}
				}
		);
	}

//	@Override
//	public IRclMediaFactory getMediaFactory() {
//		return mediaFactory;
//	}

	@Override
	public SdpHelper getSdpHelper() {
		return sdpHelper;
	}

	@Override
	public PresenceHelper getPresenceHelper() {
		return presenceHelper;
	}
	
	@Override
	public ConferenceHelper getConferenceHelper() {
		return conferenceHelper;
	}
	
	public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
		return scheduler.schedule(task, delay, timeUnit);
	}

	@Override
	public Collection<JainSipConnection> findConnectionsByPhone(final String userPhone) {
		if (globalParams.isEnableExclusiveRegister()) {
			List<JainSipConnection> ret = new ArrayList<JainSipConnection>(1);
			JainSipConnection connection = phone2connection.get(userPhone);
			if (connection!=null) {
				ret.add(connection);
			}
			return ret;
		} else {
			return CollectionUtils.subCollection(
					allconnections, 
					new CollectionUtils.Criteria<JainSipConnection>() {
						@Override
						public boolean satisfies(JainSipConnection connection) {
							return connection.getPhoneNumber().equals(userPhone);
						}
					}
			);
		}
	}
	
}
