package com.rcslabs.rcl.core;

public interface IAddressUri {

	String getProtocol();
	String getUserName();
	String getPhoneNumber();
	String getDomainName();

	void setProtocol(String protocol);
	void setUserName(String userName);
	void setPhoneNumber(String phoneNumber);
	void setDomainName(String domainName);

}
