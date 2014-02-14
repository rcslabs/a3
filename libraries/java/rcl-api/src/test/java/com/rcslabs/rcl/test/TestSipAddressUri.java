package com.rcslabs.rcl.test;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;

import javax.sip.address.Address;

import junit.framework.Assert;

import org.junit.Test;

import com.rcslabs.rcl.SipAddressUri;

public class TestSipAddressUri {

	@Test
	public void testParseUriString() {
		verifyParseUriString("1234@multifon.ru", "1234@multifon.ru");
		verifyParseUriString("sip:1234@multifon.ru", "sip:1234@multifon.ru");
		verifyParseUriString("\"Вася\"  <1234@multifon.ru>", "\"Вася\" <1234@multifon.ru>");
		verifyParseUriString("\"Вася\"  <sip:1234@multifon.ru>", "\"Вася\" <sip:1234@multifon.ru>");
		verifyParseUriString("\"Вася\"  <sip:1234-retrt@multifon.ru>", "\"Вася\" <sip:1234@multifon.ru>");
		verifyParseUriString("\"Вася\"  <tel:1234>", "\"Вася\" <tel:1234>");
	}

	@Test
	public void testToString() {
		SipAddressUri uri = new SipAddressUri();
		uri.setPhoneNumber("1234");
		Assert.assertEquals("sip:1234", uri.toString());
	}
	
	private void verifyParseUriString(String uri2parse, String expected) {
		SipAddressUri uri = new SipAddressUri(uri2parse);
		Assert.assertEquals(expected, uri.toString());
		
	}

	public void testParseSipAddress() {
		Address addr = new AddressImpl();
		SipUri uri = new SipUri();
		addr.setURI(uri);
		verifyParseUriString(addr, "");
	}

	private void verifyParseUriString(Address address2parse, String expected) {
		SipAddressUri uri = new SipAddressUri(address2parse);
		Assert.assertEquals(expected, uri.toString());
	}
}
