package com.rcslabs.rcl.core.entity;

/**
 * Modifiable connection params.
 *
 */
public interface IModifiableConnectionParams extends IConnectionParams {

	void setPhoneNumber(String phoneNumber);
	
	void setUserName(String userName);

	void setPassword(String password);

	void setPresenceEnabled(boolean presenceEnabled);

	void setSkipAuthentication(boolean skipAuthentication);

	void setServerAddress(String sipServerAddress);

	void setDomainName(String domainName);

}
