package com.rcslabs.rcl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.sip.Dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.event.PresenceEvent;
import com.rcslabs.rcl.presence.IPresenceEvent;
import com.rcslabs.rcl.presence.IPresenceListener;
import com.rcslabs.rcl.presence.IPresenceService;
import com.rcslabs.rcl.presence.entity.ContactPresence;
import com.rcslabs.rcl.presence.entity.IContactPresence;
import com.rcslabs.rcl.presence.entity.PresenceMood;
import com.rcslabs.rcl.presence.entity.PresenceStatus;

public class JainSipPresenceService extends AbstractListenable<IPresenceListener, PresenceEvent> implements IPresenceService {
	private static Logger log = LoggerFactory.getLogger(JainSipPresenceService.class);

	private class Subscription {
		private String phoneNumber;
		private String eventType;
		private Dialog dialog;
		private ScheduledFuture<?> reSubscribeFuture;

		public Subscription(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}
	}

	private final JainSipConnection connection;
	private final JainSipCallManager callManager;
	private Map<String, Subscription> subscriptions = new ConcurrentHashMap<String, Subscription>();
	private ContactPresence presence = new ContactPresence();
	private ScheduledFuture<?> rePublishFuture;

	public JainSipPresenceService(JainSipConnection connection, JainSipCallManager callManager) {
		this.connection = connection;
		this.callManager = callManager;

		presence.setStatus(PresenceStatus.OFFLINE);
	}

	@Override
	public List<IContactPresence> getContactStates() {
		return null;
	}

	@Override
	public void setMood(PresenceMood mood) {
		presence.setMood(mood);
		publishPresence();
	}

	@Override
	public void setNote(String note) {
		presence.setNote(note);
		publishPresence();
	}

	@Override
	public void setStatus(PresenceStatus status) {
		if (status == null)
			throw new IllegalArgumentException("Presence status cannot be null");
		presence.setStatus(status);
		publishPresence();
	}

	private void publishPresence() {
		if (rePublishFuture != null) {
			rePublishFuture.cancel(true);
			rePublishFuture = null;
		}
		try {
			callManager.publish(connection, presence);
		} catch (Exception e) {
			throw new RclException("Failed to publish presence for connection " + connection.getId(), e);
		}
	}

	@Override
	public void subscribeToContacts(String[] phoneNumbers) {
		if (phoneNumbers == null)
			throw new IllegalArgumentException("phoneNumbers cannot be null");

		for (String phoneNumber : phoneNumbers) {
			try {
				Subscription subscription = subscriptions.get(phoneNumber);
				Dialog dialog;
				if (subscription != null) {
					dialog = subscription.dialog;
				} else {
					dialog = null;
				}
				callManager.subscribe(phoneNumber, connection, "presence", dialog);
				subscriptions.put(phoneNumber, new Subscription(phoneNumber));
			} catch (Exception e) {
				throw new RclException("Failed to subscribe to events of " + phoneNumber, e);
			}
		}
	}

	// public void tryReSubscribe() {
	// long currentTimestamp = System.currentTimeMillis();
	//
	// for(Subscription subscription : subscriptions.values()) {
	// try {
	// long expires = (subscription.expires - 10) * 1000;
	// if(currentTimestamp - subscription.subscribeTimestamp > expires) {
	// log.info("Connection {}: subscription for phoneNumber {} has expired, re-subscribing...",
	// connection.getId(), subscription.phoneNumber);
	// callManager.subscribe(subscription.phoneNumber, connection);
	// }
	// } catch (Exception e) {
	// fireErrorEvent("Connection " + connection.getId() +
	// ": failed to re-subscribe to phone number " + subscription.phoneNumber,
	// e);
	// }
	// }
	// }

	@Override
	public void unsubscribe() {
		Map<String, Subscription> subscriptionsCopy = new ConcurrentHashMap<String, Subscription>(subscriptions);
		for (String phoneNumber : subscriptionsCopy.keySet()) {
			try {
				callManager.unsubscribe(phoneNumber, connection);
			} catch (Exception e) {
				log.error("Failed to unsibscribe from " + phoneNumber + ", subscription data was forcibly removed", e);
			} finally {
				subscriptions.remove(phoneNumber);
			}
		}
	}

	@Override
	public void unsubscribeContacts(String[] phoneNumbers) {
		if (phoneNumbers == null)
			throw new IllegalArgumentException("phoneNumbers cannot be null");

		for (String phoneNumber : phoneNumbers) {
			try {
				Subscription subscription = subscriptions.get(phoneNumber);
				if (subscription != null) {
					try {
						callManager.unsubscribe(phoneNumber, connection);
					} catch (Exception e) {
						log.error("Failed to unsibscribe from " + phoneNumber + ", subscription data was forcibly removed", e);
					} finally {
						subscriptions.remove(phoneNumber);
					}
				}
			} catch (Exception e) {
				throw new RclException("Failed to subscribe to events of " + phoneNumber, e);
			}
		}
	}

	@Override
	public String getPhoneNumber() {
		return connection.getParams().getPhoneNumber();
	}

	@Override
	public String getUri() {
		return null;
	}

	@Override
	public PresenceMood getMood() {
		return presence.getMood();
	}

	@Override
	public String getNote() {
		return presence.getNote();
	}

	@Override
	public PresenceStatus getStatus() {
		return presence.getStatus();
	}

	public void fireEvent(PresenceEvent event) {
		event.setConnection(connection);
		super.fireEvent(event);
	}

	public void fireErrorEvent(int statusCode, String errorText) {
		log.debug("Firing error: {}: {}", statusCode, errorText);
		PresenceEvent event = new PresenceEvent(IPresenceEvent.Type.PRESENCE_ERROR);
		event.setConnection(connection);
		ErrorInfo errInfo = new ErrorInfo();
		errInfo.setErrorCode(statusCode);
		errInfo.setErrorText(errorText);
		event.setErrorInfo(errInfo);
		fireEvent(event);
	}

	public void fireErrorEvent(String errorText, Exception cause) {
		log.debug("Firing error: " + errorText, cause);
		PresenceEvent event = new PresenceEvent(IPresenceEvent.Type.PRESENCE_ERROR);
		event.setConnection(connection);
		ErrorInfo errInfo = new ErrorInfo();
		errInfo.setErrorText(errorText);
		errInfo.setCause(new RclException(cause));
		event.setErrorInfo(errInfo);
		fireEvent(event);
	}

	public boolean hasSubscription(String phoneNumber) {
		return subscriptions.containsKey(phoneNumber);
	}

	public void removeSubscription(String phoneNumber) {
		Subscription subscription = subscriptions.remove(phoneNumber);
		if (subscription != null) {
			ScheduledFuture<?> currentFuture = subscription.reSubscribeFuture;
			if (currentFuture != null) {
				currentFuture.cancel(true);
			}
		}
	}

	public void setSubscriptionInfo(String phoneNumber, int expires, Dialog dialog, String eventType) {
		if ("presence.winfo".equals(eventType)) {
			// Presence inited - publish current status
			// if(connection.isPresenceEnabled()) {
			if (expires > 0) {
				// if(getStatus() == PresenceStatus.OFFLINE) {
				setStatus(PresenceStatus.ONLINE);
			}
			// }
		}
		if (subscriptions.containsKey(phoneNumber)) {
			final Subscription subscription = subscriptions.get(phoneNumber);
			subscription.dialog = dialog;
			subscription.eventType = eventType;
			ScheduledFuture<?> currentFuture = subscription.reSubscribeFuture;
			if (currentFuture != null) {
				currentFuture.cancel(true);
			}
			subscription.reSubscribeFuture = callManager.schedule(new Runnable() {
				@Override
				public void run() {
					subscription.reSubscribeFuture = null;
					try {
						callManager.subscribe(subscription.phoneNumber, connection, subscription.eventType, subscription.dialog);
					} catch (Exception e) {
						log.error("Failed to re-subscribe for connection " + JainSipPresenceService.this.connection.getId() + " to "
								+ subscription.phoneNumber, e);
						JainSipPresenceService.this.fireErrorEvent("Failed to re-subscribe to " + subscription.phoneNumber, e);
					}
				}
			}, expires - JainSipPresenceService.this.callManager.getGlobalParams().getSubscribeBeforeExpirationTime(), TimeUnit.SECONDS);
		}
	}

	public void onSuccessfulPublish(long expires) {
		// if (disconnecting) {
		// return;
		// }
		log.debug("Connection " + this.connection.getId() + " successfully published its status.");

		ScheduledFuture<?> future = this.rePublishFuture;
		if (future != null) {
			future.cancel(true);
			future = null;
		}
		this.rePublishFuture = callManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					callManager.publish(connection, presence);
				} catch (Exception e) {
					log.error("Failed to publish presence for connection " + JainSipPresenceService.this.connection.getId(), e);
					JainSipPresenceService.this.fireErrorEvent("Failed to re-publish", e);
				}
			}
		}, expires - JainSipPresenceService.this.callManager.getGlobalParams().getPublishBeforeExpirationTime(), TimeUnit.SECONDS);
	}
}
