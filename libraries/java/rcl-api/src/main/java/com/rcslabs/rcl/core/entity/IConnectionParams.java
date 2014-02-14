package com.rcslabs.rcl.core.entity;

public interface IConnectionParams {

	IModifiableConnectionParams clone();
	
	String getUserName();

	String getPhoneNumber();

	String getPassword();
	
	boolean isPresenceEnabled();
	
	boolean isSkipAuthentication();
	
	String getServerAddress();
	
	String getProxyAddress();

	String getDomainName();
}