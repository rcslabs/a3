package com.rcslabs.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for IP addresses.
 *
 */
public class IpAddressUtils {
	private static Logger log = LoggerFactory.getLogger(IpAddressUtils.class);
	
	private IpAddressUtils() {}

	/**
	 * Determines the local IPv4 address by looking through the available
	 * network interfaces. Skips the loopback (127.0.0.1).
	 * If there are several sutable interfaces, it chooses one of them,
	 * and this choice is not guaranteed to be the same each time.
	 * Returns null, if no suitable interface is found. 
	 */
	public static String getLocalIpAddress() {
		try {
			log.debug("Enumerating network interfaces...");
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			LinkedList<String> sorted = new LinkedList<String>();

			while(interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				log.debug("Network interface: {}", ni.getName());
				Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
				while(inetAddresses.hasMoreElements()) {
					InetAddress ip = inetAddresses.nextElement();
					log.debug("Address: {}", ip.getHostAddress());
					if(!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) { //not loopback and not IPv6
						log.debug("Interface {} seems to be the right one.", ni.getName());
						if(0 == ip.getHostAddress().indexOf("192.168.")){
							sorted.addFirst( ip.getHostAddress() );
						}else{
							sorted.addLast( ip.getHostAddress() );
						}
					}
				}
			}
			
			if(0 != sorted.size()){
				return sorted.get(0);
			}
			
		} catch (SocketException e) {
			log.error("Failed to get local IPv4 address", e);
			return null;
		}
		
		log.debug("No suitable interface found, returning null");
		return null;
	}
}
