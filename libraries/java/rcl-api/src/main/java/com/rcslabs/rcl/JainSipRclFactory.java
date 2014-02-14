package com.rcslabs.rcl;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.rcl.telephony.entity.CallParams;
import com.rcslabs.util.CollectionUtils.Criteria;
import com.rcslabs.util.CollectionUtils.Operation;

/**
 * An entry point for this implementation of RCL API.
 * 
 * This implementation uses SIP (NIST-SIP stack) to establish and control
 * connections and calls.
 * 
 * Some implementation details:
 * 
 * <ul>
 * 	<li>If ConnectionParams.skipAuthentication is true - the REGISTER won't be sent.</li>
 * </ul>
 *
 */
public class JainSipRclFactory implements IRclFactory {
	private static Logger log = LoggerFactory.getLogger(JainSipRclFactory.class);
	private JainSipCallManager callManager;
	private List<WeakReference<IConnection>> connectionObjects = new LinkedList<WeakReference<IConnection>>();
	private ReferenceQueue<IConnection> reclaimedObjects = new ReferenceQueue<IConnection>();
	private final JainSipGlobalParams globalParams;
	
	public JainSipRclFactory(JainSipGlobalParams params) {
		this.globalParams = (JainSipGlobalParams)params.clone();
		this.callManager = new JainSipCallManager(this.globalParams);
	}

	public JainSipCall newCall(JainSipConnection connection) {
		return new JainSipCall(connection, callManager);
	}
	
	public JainSipCall newCall(JainSipConnection connection, CallParams params) {
		return new JainSipCall(connection, callManager, params);
	}

	public synchronized IConnection newConnection() {
		if(!callManager.isStarted()) { //first connection
			callManager.start();		
		}
		JainSipConnection ret = new JainSipConnection(this, callManager);
		connectionObjects.add(new WeakReference<IConnection>(ret, reclaimedObjects));
		checkReclaimedObjects();
		return ret;
	}
	
	private void checkReclaimedObjects() {
		Reference<? extends IConnection> r = null;
		while((r = reclaimedObjects.poll()) != null) {
			connectionObjects.remove(r);
		}
		
		if(globalParams.isAutomaticDispose() && connectionObjects.size() == 0) { //no more connections
			log.info("Automatic dispose and all connections removed: disposing...");
			dispose();
		}
	}
	
	synchronized void release(IConnection connection) {
		checkReclaimedObjects();
		
		for(int i = 0; i < connectionObjects.size(); i++) {
			if(connectionObjects.get(i).get() == connection) {
				connectionObjects.remove(i);
				
				if(globalParams.isAutomaticDispose() && connectionObjects.size() == 0) { //no more connections
					log.info("Automatic dispose and all connections removed: disposing...");
					dispose();
				}
				
				return;
			}
		}
	}
	
	public void dispose() {
		globalParams.setAutomaticDispose(false);
		List<WeakReference<IConnection>> disposeConnections = 
				new LinkedList<WeakReference<IConnection>>(connectionObjects);
		for(WeakReference<IConnection> connectionRef : disposeConnections) {
			try {
				IConnection connection = connectionRef.get();
				if(connection != null) {
					connection.close();
				}
			}
			catch(Exception e) {
				log.warn("connection.close() threw exception during factory dispose", e); 
			}
		}
		
		callManager.stop();
	}
	
	JainSipGlobalParams getGlobalParams() {
		return globalParams;
	}
	
	@Override
	public IConnection findConnection(String id) {
		return callManager.getConnectionById(id);
	}
	
	@Override
	public void forEachConnection(
			Criteria<IConnection> criteria, 
			Operation<IConnection> operation) 
	{
		callManager.forEachConnectionInPool(criteria, operation);
	}
	
	//this method is made for testing purposes
	public boolean isStarted() {
		return callManager.isStarted();
	}
	
	@Override
	public String toString() {
		return "JainSipRclFactory [globalParams=" + globalParams + ", mediaFactory=" + "]";
	}

	public final JainSipCallManager getCallManager() {
		return callManager;
	}

}
