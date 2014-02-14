package ru.rcslabs.webcall.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;

public class WebConnectionInfo {
	private Map<String, String> data = new HashMap<String, String>();
	
	public static final String IP_ADDRESS = "ip-address";
	public static final String IP_PORT = "ip-port";
	public static final String IP_PROTOCOL = "ip-protocol";
	public static final String USER_AGENT = "user-agent";
	public static final String FLASH_VERSION = "flash-version";
	public static final String REFERER = "referer";

	public String getData(String key) {
		return data.get(key);
	}
	
	public void setData(String key, String value) {
		data.put(key, value);
	}
	
	public void putToMDC() {
		for(String key : data.keySet()) {
			MDC.put(key, data.get(key));
		}
	}
}
