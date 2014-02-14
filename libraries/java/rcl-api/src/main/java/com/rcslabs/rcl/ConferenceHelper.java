package com.rcslabs.rcl;

import ietf.params.xml.ns.conference_info.ConferenceType;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ConferenceHelper {
	
	private ietf.params.xml.ns.conference_info.ObjectFactory ciof = 
			new ietf.params.xml.ns.conference_info.ObjectFactory();

	private JAXBContext jaxbContext;
	private Unmarshaller unmarshaller;
	
	public ConferenceHelper() {
		try {
			jaxbContext = JAXBContext.newInstance(ciof.getClass());
			unmarshaller = jaxbContext.createUnmarshaller();
		}
		catch(Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ConferenceType unmarshal(byte[] rawContent) throws JAXBException {
		ByteArrayInputStream input = new ByteArrayInputStream(rawContent);
		ConferenceType ret;
		synchronized(this) {
			ret = ((JAXBElement<ConferenceType>)unmarshaller.unmarshal(input)).getValue();
		}
		
		return ret;
	}
}
