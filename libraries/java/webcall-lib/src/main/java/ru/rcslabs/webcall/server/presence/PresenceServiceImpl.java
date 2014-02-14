package ru.rcslabs.webcall.server.presence;

import java.util.EnumSet;

import ru.rcslabs.webcall.server.WebcallApplicationService;
import ru.rcslabs.webcall.server.api.IWebcallPresenceService;
import ru.rcslabs.webcall.server.app.IWebcallApplication;
import ru.rcslabs.webcall.server.event.adapter.PresenceEventListenerAdapter;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.presence.IPresenceService;
import com.rcslabs.rcl.presence.entity.PresenceMood;
import com.rcslabs.rcl.presence.entity.PresenceStatus;
import com.rcslabs.util.CollectionUtils;

public class PresenceServiceImpl extends WebcallApplicationService 
implements IWebcallPresenceService, IConnectionListener {
	
	@Override
	public void setApplication(IWebcallApplication context) {
		if (context==null) {
			if (this.getApplication()!=null) {
				this.getApplication().getDispatcher().removeListener(this);
			}
		} else {
			context.getDispatcher().addListener(this);
		}
		super.setApplication(context);
	}

	@Override
	public void publishMood(String uid, String mood) throws ServiceNotEnabledException {
		IConnection conn = getConnection(uid);
		IPresenceService impl = conn.getService(IPresenceService.class);
		impl.setMood(string2mood(mood));
	}

	private PresenceMood string2mood(final String mood) {
		return CollectionUtils.findSingle(
				EnumSet.allOf(PresenceMood.class), 
				new CollectionUtils.Criteria<PresenceMood>() {
					@Override
					public boolean satisfies(PresenceMood element) {
						return element.toString().equals(mood);
					}
				},
				PresenceMood.UNKNOWN
		);
	}

	@Override
	public void publishStatus(String uid, String status) throws ServiceNotEnabledException {
		IConnection conn = getConnection(uid);
		IPresenceService impl = conn.getService(IPresenceService.class);
		impl.setStatus(string2status(status));
	}
	
	private PresenceStatus string2status(final String status) {
		return CollectionUtils.findSingle(
				EnumSet.allOf(PresenceStatus.class), 
				new CollectionUtils.Criteria<PresenceStatus>() {
					@Override
					public boolean satisfies(PresenceStatus element) {
						return element.toString().equals(status);
					}
				},
				PresenceStatus.ONLINE
		);
	}

	@Override
	public void publishNote(String uid, String note) throws ServiceNotEnabledException {
		IConnection conn = getConnection(uid);
		IPresenceService impl = conn.getService(IPresenceService.class);
		impl.setNote(note);
	}

	/*
	 * ------------- ICallObjectsLifetimeListener implementation --------------
	 */
	@Override
	public void onConnectionCreated(IConnection conn) {
		conn.addListener(this);
		if(conn.isServiceEnabled(IPresenceService.class)) {
			try {
				IPresenceService serv =	conn.getService(IPresenceService.class);
				serv.addListener(PresenceEventListenerAdapter.DEFAULT_INSTANCE);
			} catch (ServiceNotEnabledException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/*
	 * ------------- IConnectionListener implementation --------------
	 */
	@Override
	public void onConnecting(IConnectionEvent event) {
		// Do nothing
	}


	@Override
	public void onConnected(IConnectionEvent event) {
		IConnection conn = event.getConnection();
		if(conn.isServiceEnabled(IPresenceService.class)) {
			try {
				IPresenceService serv = conn.getService(IPresenceService.class);
				serv.subscribeToContacts(new String[] {conn.getParams().getPhoneNumber()+"_all"});
			}
			catch(ServiceNotEnabledException e) {
				assert(false); //never happens
			}
		}
	}


	@Override
	public void onConnectionBroken(IConnectionEvent event) {
		// Do nothing
	}


	@Override
	public void onConnectionFailed(IConnectionEvent event) {
		// Do nothing
	}


	@Override
	public void onConnectionError(IConnectionEvent event) {
		// Do nothing
	}

}
