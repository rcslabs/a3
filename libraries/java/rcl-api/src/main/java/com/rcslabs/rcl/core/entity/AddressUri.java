package com.rcslabs.rcl.core.entity;

import com.rcslabs.rcl.core.IAddressUri;

public class AddressUri implements IAddressUri {

	private String protocol;
	private String userName;
	private String phoneNumber;
	private String domain;
	
	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override
	public String getDomainName() {
		return domain;
	}

	@Override
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public void setDomainName(String domainName) {
		this.domain = domainName;
	}

	@Override
	public String toString() {
		String protocol = this.protocol==null ? "" : this.protocol+":";
		String domain = this.domain==null? "" : "@"+this.getDomainName();
		if (this.getUserName()==null) {
			return protocol+phoneNumber+domain;
		} else {
			return "\""+this.getUserName()+"\" <"+protocol+phoneNumber+domain+">";
		}
	}
	
}
