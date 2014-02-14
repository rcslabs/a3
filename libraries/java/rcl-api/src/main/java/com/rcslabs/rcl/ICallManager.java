package com.rcslabs.rcl;

import java.util.Collection;

import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import com.rcslabs.util.CollectionUtils;

public interface ICallManager {
	public static final String VIA_TRANSPORT = "UDP";
	public static final String VIA_PREFIX = "z9hG4bK";
	public static final String APPDATA_AUTH_HEADER = "auth-header";
	
	JainSipGlobalParams getGlobalParams();

	MessageFactory getMessageFactory();
	
	HeaderFactory getHeaderFactory();
	
	AddressFactory getAddressFactory();
	
	SipProvider getProvider();
	
//	IRclMediaFactory getMediaFactory();
	
	VersionHelper getVersionHelper();
	
	SdpHelper getSdpHelper();
	
	PresenceHelper getPresenceHelper();

	void addConnectionToPool(JainSipConnection connection);
	void removeConnectionFromPool(JainSipConnection connection);
	Collection<JainSipConnection> findConnectionsByPhone(String userPhone);
	
	void forEachConnectionInPool(CollectionUtils.Operation<JainSipConnection> operation); 

	void initPresence(JainSipConnection connection) throws Exception;

	void addCallToPool(String id, JainSipCall call);
	void removeCallFromPool(String id);
	JainSipCall getCallFromPool(String id);

	ConferenceHelper getConferenceHelper();

}
