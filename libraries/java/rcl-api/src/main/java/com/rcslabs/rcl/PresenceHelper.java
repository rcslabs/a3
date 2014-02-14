package com.rcslabs.rcl;

import ietf.params.xml.ns.pidf.Basic;
import ietf.params.xml.ns.pidf.Contact;
import ietf.params.xml.ns.pidf.Presence;
import ietf.params.xml.ns.pidf.Status;
import ietf.params.xml.ns.pidf.Tuple;
import ietf.params.xml.ns.pidf.data_model.NoteT;
import ietf.params.xml.ns.pidf.data_model.Person;
import ietf.params.xml.ns.pidf.rpid.Activities;
import ietf.params.xml.ns.pidf.rpid.Empty;
import ietf.params.xml.ns.pidf.rpid.Mood;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.presence.entity.ContactPresence;
import com.rcslabs.rcl.presence.entity.IContactPresence;
import com.rcslabs.rcl.presence.entity.PresenceMood;
import com.rcslabs.rcl.presence.entity.PresenceStatus;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Performs presence XML <-> POJO conversion with JAXB.
 *
 */
public class PresenceHelper {
	private Logger log = LoggerFactory.getLogger(PresenceHelper.class);
	
	private ietf.params.xml.ns.pidf.ObjectFactory pidfof = new ietf.params.xml.ns.pidf.ObjectFactory();
	private ietf.params.xml.ns.pidf.rpid.ObjectFactory rpidof = new ietf.params.xml.ns.pidf.rpid.ObjectFactory();
	private ietf.params.xml.ns.pidf.data_model.ObjectFactory dmof = new ietf.params.xml.ns.pidf.data_model.ObjectFactory();

	private JAXBContext jaxbContext;
	private Unmarshaller unmarshaller;
	private Marshaller marshaller;
	
	private static class PresenceNamespacePrefixMapper extends NamespacePrefixMapper {

		@Override
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
			if("urn:ietf:params:xml:ns:pidf:data-model".equals(namespaceUri)) {
				return "dm";
			}
			else if("urn:ietf:params:xml:ns:pidf:rpid".equals(namespaceUri)) {
				return "rpid";
			}
			else if("urn:ietf:params:xml:ns:pidf".equals(namespaceUri) || 
					"http://www.w3.org/XML/1998/namespace".equals(namespaceUri)) 
			{
				return ""; //default namespace
			}
			else {
				throw new IllegalArgumentException("Unknown namespace " + namespaceUri + " for presence XML.");
			}
		}
		
	}

	public PresenceHelper() {
		try {
			jaxbContext = JAXBContext.newInstance(
					pidfof.getClass(), 
					rpidof.getClass(),
					dmof.getClass());
			unmarshaller = jaxbContext.createUnmarshaller();
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new PresenceNamespacePrefixMapper());
		}
		catch(Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ContactPresence unmarshal(byte[] rawContent) throws JAXBException {
		ByteArrayInputStream input = new ByteArrayInputStream(rawContent);
		Presence presence;
		synchronized(this) {
			presence = ((JAXBElement<Presence>)unmarshaller.unmarshal(input)).getValue();
		}
		
		return toContactPresence(presence);
	}

	private ContactPresence toContactPresence(Presence presence) {
		ContactPresence ret = new ContactPresence();
		Person person = findObjectInList(presence.getAny(), Person.class);
		
		//set presence status
		if(presence.getTuple().size() > 0) {
			ret.setStatus(
				toPresenceStatus(
						presence.getTuple().get(0).getStatus(),
						person
				)
			);
		}
		
		//set mood
		if(person != null) {
			ret.setMood(
					toPresenceMood(person)
			);
			
			List<NoteT> note = person.getNote();
			ret.setNote(note.size() > 0 ? note.get(0).getValue() : null);
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	private PresenceMood toPresenceMood(Person person) {
		Mood mood = findObjectInList(person.getAny(), Mood.class);
		
		if(mood != null) {
			for(Object element : mood.getAfraidOrAmazedOrAngry()) {
				JAXBElement<Empty> moodElement = (JAXBElement<Empty>)element;
				String localName = moodElement.getName().getLocalPart();
				
				try {
					return PresenceMood.valueOf(localName.toUpperCase());
				}
				catch(IllegalArgumentException e) {
					//ignore and continue loop. This could be optimized.
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	private PresenceStatus toPresenceStatus(Status status, Person person) {
		PresenceStatus ret = PresenceStatus.OFFLINE;
		if(status != null && status.getBasic() != null) {
			if(Basic.OPEN.value().equals(status.getBasic().value())) {
				ret = PresenceStatus.ONLINE;
			}
		}
		
		if(person != null) {
			Activities activities = findObjectInList(person.getAny(), Activities.class);
			if(activities != null) {
				for(Object activity : activities.getAppointmentOrAwayOrBreakfast()) {
					JAXBElement<Empty> activityElement = (JAXBElement<Empty>)activity;
					String localName = activityElement.getName().getLocalPart();
					
					if("away".equals(localName)) {
						ret = PresenceStatus.AWAY;
					}
					else if("busy".equals(localName)) {
						ret = PresenceStatus.BUSY;
					}
				}
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T findObjectInList(List<Object> list, Class<T> cls) {
		for(Object o : list) {
			if(cls.isInstance(o)) {
				return (T)o;
			}
		}
		return null;
	}
	
	public String marshal(IContactPresence presence, String presenceUri) throws JAXBException {
		Presence xmlPresence = toXmlPresence(presence, presenceUri);
		StringWriter writer = new StringWriter();
		try {
			synchronized(this) {
				marshaller.marshal(pidfof.createPresence(xmlPresence), writer);
			}
			return writer.toString();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}

	private Presence toXmlPresence(IContactPresence presence, String presenceUri) {
		Presence ret = pidfof.createPresence();
		ret.setEntity(presenceUri);
		
		//set basic status and contact
		Tuple tuple = pidfof.createTuple();
		tuple.setId(UUID.randomUUID().toString());
		Status status = pidfof.createStatus();
		status.setBasic(toBasicStatus(presence.getStatus()));
		tuple.setStatus(status);
		Contact contact = pidfof.createContact();
		contact.setValue(presenceUri);
		tuple.setContact(contact);
		
		ret.getTuple().add(tuple);
		
		Person person = dmof.createPerson();
		person.setId(UUID.randomUUID().toString());
		
		//set mood
		JAXBElement<Empty> moodElement = toRpidMood(presence.getMood());
		if(moodElement != null) {
			Mood mood = rpidof.createMood();
			mood.getAfraidOrAmazedOrAngry().add(moodElement);
			person.getAny().add(mood);
		}
		
		//set activity
		JAXBElement<Empty> activityElement = toRpidActivity(presence.getStatus());
		if(activityElement != null) {
			Activities activities = rpidof.createActivities();
			activities.getAppointmentOrAwayOrBreakfast().add(activityElement);
			person.getAny().add(activities);
		}
		
		if(presence.getNote() != null) {
			NoteT note = dmof.createNoteT();
			note.setValue(presence.getNote());
			person.getNote().add(note);
		}
		
		ret.getAny().add(person);
		
		return ret;
	}

	private JAXBElement<Empty> toRpidActivity(PresenceStatus status) {
		Empty empty = rpidof.createEmpty();
		
		switch(status) {
		case ONLINE:
		case OFFLINE:
			break;
			
		case BUSY:
			return rpidof.createActivitiesBusy(empty);
			
		case AWAY:
			return rpidof.createActivitiesAway(empty);
			
		default:
			log.warn("Unknown/unimplemented presence status {}", status);
			break;
		}
		
		return null;
	}

	private JAXBElement<Empty> toRpidMood(PresenceMood mood) {
		if(mood == null) return null;
		Empty empty = rpidof.createEmpty();
		
		switch(mood) {
		case UNKNOWN:
			break;
			
		case ANGRY:
			return rpidof.createMoodAngry(empty);
			
		case ASHAMED:
			return rpidof.createMoodAshamed(empty);
			
		case EXCITED:
			return rpidof.createMoodExcited(empty);
			
		case HAPPY:
			return rpidof.createMoodHappy(empty);
			
		case IN_LOVE:
			return rpidof.createMoodInLove(empty);
			
		case SAD:
			return rpidof.createMoodSad(empty);
			
		case SLEEPY:
			return rpidof.createMoodSleepy(empty);
			
		default:
			log.warn("Unknown/unimplemented mood {}", mood);
			break;
		}
		
		return null;
	}

	private Basic toBasicStatus(PresenceStatus status) {
		return status != PresenceStatus.OFFLINE ? Basic.OPEN : Basic.CLOSED;
	}

}
