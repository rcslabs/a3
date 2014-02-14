package ru.rcslabs.webcall.server;
/**
 * Supported address uri formats are<br/>
 * <li>["displayName"] userInfo
 * <li>[displayName] &lt;userInfo&gt;
 * <li>[displayName] &lt;scheme:userInfo&gt;
 * <li>[displayName] &lt;scheme:userInfo@host&gt;
 * @author dzernov
 *
 */
public class AddressUri {

	private String displayName;
	private String userInfo;
	private String host;
	private String scheme;
	
	public AddressUri(String displayName, String address) {
		this.displayName = displayName;
		parseAddress(address);
	}
	
	public AddressUri(String address) {
		// Process extended number (userName)
		address = address.trim();
		
		int index0 = address.indexOf('<', 0);
		if (index0>0) {
			String name = address.substring(0, index0);
			name = name.trim();
			int index1 = name.indexOf('"', 0);
			int index2 = name.indexOf('"', index1+1);
			if (index1>=0 && index2>index1) {
				displayName = name.substring(index1 + 1, index2);
			} else {
				if (name.isEmpty()) {
					displayName = null;
				} else {
					displayName = name;
				}
			}
			address = address.substring(index0);
		} else {
			int index1 = address.indexOf('"', 0);
			int index2 = address.indexOf('"', index1+1);
			if (index1>=0 && index2>index1) {
				displayName = address.substring(index1 + 1, index2);
				address = address.substring(index2 + 1);
			} else {
				displayName = null;
			}
			address = address.trim();
		}
		parseAddress(address);
	}
	
	private void parseAddress(String address) {
		// Remove < and >
		int index1 = address.indexOf('<');
		int index2 = address.indexOf('>');
		if (index1>=0 && index2>index1) {
			address = address.substring(index1 + 1, index2);
		}
		address = address.trim();
		
		// Process protocol
		if (address.startsWith("sip:")) {
			address = address.substring(4);
			scheme = "sip";
		} else if (address.startsWith("msisdn:+")) {
			address = address.substring(8);
			scheme = "tel";
		} else if (address.startsWith("msisdn:")) {
			address = address.substring(7);
			scheme = "tel";
		} else if (address.startsWith("tel:")) {
			address = address.substring(4);
			scheme = "tel";
		}
		
		// Get domain
		index1 = address.indexOf('@');
		if (index1>=0) {
			host = address.substring(index1+1);
			userInfo = address.substring(0, index1);
		} else {
			host = null;
			userInfo = address;
		}
	}
	
	public String getAddress() {
		StringBuilder sb = new StringBuilder();
		if (scheme!=null) {
			sb.append(scheme);
			sb.append(':');
		}
		sb.append(userInfo);
		if (host!=null) {
			sb.append('@');
			sb.append(host);
		}
		return sb.toString();
	}

	public String getUserInfo() {
		return userInfo;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getHost() {
		return host;
	}
	
	public String getUri() {
		StringBuilder sb = new StringBuilder();
		if (displayName!=null) {
			boolean containsSpaces = displayName.indexOf('"')>=0;
			if (containsSpaces) {
				sb.append('"');
			}
			sb.append(displayName);
			if (containsSpaces) {
				sb.append('"');
			}
			sb.append(" <");
		}
		if (scheme!=null) {
			sb.append(scheme);
			sb.append(':');
		}
		sb.append(userInfo);
		if (host!=null) {
			sb.append('@');
			sb.append(host);
		}
		if (displayName!=null) {
			sb.append('>');
		}
		return sb.toString();
	}

}
