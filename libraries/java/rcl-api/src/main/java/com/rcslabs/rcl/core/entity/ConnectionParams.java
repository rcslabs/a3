package com.rcslabs.rcl.core.entity;


/**
 * Parameters of the user connection to a server. These are the following:
 * 
 * <ul>
 * 	<li>phoneNumber - required, a phone number</li>
 * 	<li>userName - optional, a user name</li>
 * 	<li>password - optional, a password</li>
 * 	<li>skipAuthentication - if true, no authentication on 
 * the call server will be performed (no REGISTER will be sent in 
 * sip-implementation, but Authentication Required requests will 
 * be performed properly)</li>
 * 	<li>serverAddress - server address ("hostname[:port]"). 
 * If not specified, the global setting is taken.</li>
 * 	<li>proxyAddress - proxy server address ("hostname[:port]").
 * If not specified, the global setting is taken.</li>
 * 	<li>enablePresence - enable presence features</li>
 * </ul>
 */
public class ConnectionParams implements Cloneable, IModifiableConnectionParams {
	private String userName;
	private String phoneNumber;
	private String password;
	private boolean skipAuthentication = false;
	private String serverAddress;
	private String proxyAddress;
	private boolean presenceEnabled = true;
	private String domainName;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setSkipAuthentication(boolean skipAuthentication) {
		this.skipAuthentication = skipAuthentication;
	}
	public boolean isSkipAuthentication() {
		return skipAuthentication;
	}
	
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	public String getServerAddress() {
		return serverAddress;
	}
	public void setProxyAddress(String proxyAddress) {
		this.proxyAddress = proxyAddress;
	}
	public String getProxyAddress() {
		return proxyAddress;
	}
	public void setPresenceEnabled(boolean presenceEnabled) {
		this.presenceEnabled = presenceEnabled;
	}
	public boolean isPresenceEnabled() {
		return presenceEnabled;
	}
	@Override
	public ConnectionParams clone() {
		try {
			return (ConnectionParams)super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public String toString() {
		return String.format(
				"<%s> %s", 
				userName, 
				phoneNumber + (domainName != null ? domainName : "")
		);
	}
	@Override
	public String getDomainName() {
		return domainName;
	}
	@Override
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
}
