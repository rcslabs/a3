package com.rcslabs.rcl;

import javax.sip.address.Address;

import com.rcslabs.rcl.core.entity.AddressUri;
import com.rcslabs.rcl.exception.RclException;

public class SipAddressUri extends AddressUri {

	public SipAddressUri() {
		this.setProtocol("sip");
	}
	public SipAddressUri(Address address) {
		this.setDomainName(getDomainName(address.toString()));
		this.setProtocol(address.getURI().getScheme());
		this.setPhoneNumber(getPhoneNumber(address.toString()));
		this.setUserName(address.getDisplayName());
	}
	public SipAddressUri(String uri) {
		this.setDomainName(getDomainName(uri));
		this.setProtocol(getProtocol(uri));
		this.setPhoneNumber(getPhoneNumber(uri));
		this.setUserName(getUserName(uri));
	}

	/*
	 * From the string:
	 * 
	 * <sip:phoneNumber-magicNumber@domain.com>
	 * 
	 * we take phoneNumber. (-magicNumber is optional)
	 * 
	 * Also works for:
	 * 
	 * sip:phoneNumber@domain.com
	 * 
	 * tel:phoneNumber
	 */
	private static String getPhoneNumber(String uri) {
		try {
			String ret = uri;
			int startIndex = ret.indexOf('<');
			if (startIndex>=0) {
				ret = ret.substring(startIndex+1);
			}
			
			if(ret.startsWith("sip:")) {
				int endInd = ret.indexOf("@");
				if(endInd == -1) {
					endInd = ret.length();
				}
				String telPart = ret.substring("sip:".length(), endInd);
				
				int hyphenInd = telPart.indexOf("-");
				if(hyphenInd >= 0) {
					return telPart.substring(0, hyphenInd);
				}
				else {
					return telPart;
				}
			}
			else if(ret.startsWith("tel:")) {
				ret = ret.substring("tel:".length());
				if(ret.endsWith(">")) {
					ret = ret.substring(0, ret.length() - ">".length());
				}
				
				return ret;
			}
			else {
				int endInd = ret.indexOf("@");
				if(endInd == -1) {
					endInd = ret.indexOf('>');
					if (endInd == -1) {
						endInd = ret.length();
					}
				}
				return ret.substring(0, endInd);
			}
		}
		catch(Exception e) {
			throw new RclException("Failed to parse phone number from URI: " + uri, e);
		}
	}
	
	/*
	 * From the string:
	 * 
	 * <sip:phoneNumber-magicNumber@domain.com>
	 * 
	 * we take domain.com or return null if there is no domain.
	 */
	private static String getDomainName(String uri) {
		int startInd = uri.indexOf("@");
		if(startInd == -1) {
			return null;
		}
		int endInd = uri.indexOf('>');
		if (endInd == -1) {
			return uri.substring(startInd + 1);
		} else {
			return uri.substring(startInd + 1, endInd);
		}
	}

	/*
	 * From the string:
	 * 
	 * <sip:phoneNumber-magicNumber@domain.com>
	 * 
	 * we take domain.com or return null if there is no domain.
	 */
	private static String getProtocol(String uri) {
		int startInd = uri.indexOf("<");
		int endInd = uri.indexOf(":");
		if(endInd == -1) {
			return null;
		}
		return uri.substring(startInd + 1, endInd);
	}

	/*
	 * From the string:
	 * 
	 * <sip:phoneNumber-magicNumber@domain.com>
	 * 
	 * we take domain.com or return null if there is no domain.
	 */
	private static String getUserName(String uri) {
		int startInd = uri.indexOf('\"');
		if (startInd==-1) {
			return null;
		} else {
			int endInd = uri.indexOf('\"', startInd+1);
			if(endInd == -1) {
				return null;
			}
			return uri.substring(startInd + 1, endInd);
		}
	}

}
