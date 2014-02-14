package ru.rcslabs.webcall;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import ru.rcslabs.webcall.server.AddressUri;

public class TestAddressUri {

    @Test
    public void DecodeUriTests() throws Exception {
    	Map<String, String> uris = new HashMap<String, String>();
    	uris.put("wasa", "wasa");
    	uris.put("wasa <sip:2000>", "wasa <sip:2000>");
    	uris.put("tel:79219879999", "tel:79219879999");
    	uris.put("sip:blabla@multifon.ru", "sip:blabla@multifon.ru");
    	uris.put("\"french\" <sip:blabla@multifon.ru>", "french <sip:blabla@multifon.ru>");
    	uris.put("\"french\" <sip:5678>", "french <sip:5678>");
    	
    	for (String sUri : uris.keySet()) {
	    	AddressUri cUri = new AddressUri(sUri); 
	    	Assert.assertEquals(uris.get(sUri), cUri.getUri());
    	}
    }

    @Test
    public void DecodeAddressTests() throws Exception {
    	Map<String, String> uris = new HashMap<String, String>();
    	uris.put("wasa", "wasa");
    	uris.put("wasa <sip:2000>", "sip:2000");
    	uris.put("tel:79219879999", "tel:79219879999");
    	uris.put("sip:blabla@multifon.ru", "sip:blabla@multifon.ru");
    	uris.put("\"french\" <sip:blabla@multifon.ru>", "sip:blabla@multifon.ru");
    	uris.put("\"french\" <sip:5678>", "sip:5678");
    	
    	for (String sUri : uris.keySet()) {
	    	AddressUri cUri = new AddressUri(sUri); 
	    	Assert.assertEquals(uris.get(sUri), cUri.getAddress());
    	}
    }

    @Test
    public void DecodeDisplayNameTests() throws Exception {
    	Map<String, String> uris = new HashMap<String, String>();
    	uris.put("wasa", null);
    	uris.put("wasa <sip:2000>", "wasa");
    	uris.put("tel:79219879999", null);
    	uris.put("sip:blabla@multifon.ru", null);
    	uris.put("\"french\" <sip:blabla@multifon.ru>", "french");
    	uris.put("\"french\" <sip:5678>", "french");
    	
    	for (String sUri : uris.keySet()) {
	    	AddressUri cUri = new AddressUri(sUri); 
	    	Assert.assertEquals(uris.get(sUri), cUri.getDisplayName());
    	}
    }
}