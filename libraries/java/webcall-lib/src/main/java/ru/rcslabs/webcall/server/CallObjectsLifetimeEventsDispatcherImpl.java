package ru.rcslabs.webcall.server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.telephony.ICall;

public class CallObjectsLifetimeEventsDispatcherImpl implements ICallObjectsLifetimeEventsDispatcher {

	private static Logger log = LoggerFactory.getLogger(CallObjectsLifetimeEventsDispatcherImpl.class);
	
	private Set<ICallObjectsLifetimeListener> listeners = new HashSet<ICallObjectsLifetimeListener>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	@Override
	public void addListener(ICallObjectsLifetimeListener listener) {
		Lock l = lock.writeLock();
		l.lock();
		try {
			listeners.add(listener);
		} finally {
			l.unlock();
		}
	}

	@Override
	public void removeListener(ICallObjectsLifetimeListener listener) {
		Lock l = lock.writeLock();
		l.lock();
		try {
			listeners.remove(listener);
		} finally {
			l.unlock();
		}
	}

	@Override
	public void fireConnectionCreated(IConnection conn) {
		Lock l = lock.readLock();
		l.lock();
		try {
			for(ICallObjectsLifetimeListener listener : listeners) {
				try {
					listener.onConnectionCreated(conn);
				} catch(Exception e) {
					log.error("Got exception from listener. Exception skipped.", e);
				}
			}
		} finally {
			l.unlock();
		}
	}

	@Override
	public void fireConnectionDestroyed(IConnection conn, String uid, IClientConnection client) {
		Lock l = lock.readLock();
		l.lock();
		try {
			for(ICallObjectsLifetimeListener listener : listeners) {
				try {
					listener.onConnectionDestroyed(conn, uid, client);
				} catch(Exception e) {
					log.error("Got exception from listener. Exception skipped.", e);
				}
			}
		} finally {
			l.unlock();
		}
	}

	@Override
	public void fireCallCreated(ICall call) {
		Lock l = lock.readLock();
		l.lock();
		try {
			for(ICallObjectsLifetimeListener listener : listeners) {
				try {
					listener.onCallCreated(call);
				} catch(Exception e) {
					log.error("Got exception from listener. Exception skipped.", e);
				}
			}
		} finally {
			l.unlock();
		}
	}

	@Override
	public void fireCallDestroyed(ICall call) {
		Lock l = lock.readLock();
		l.lock();
		try {
			for(ICallObjectsLifetimeListener listener : listeners) {
				try {
					listener.onCallDestroyed(call);
				} catch(Exception e) {
					log.error("Got exception from listener. Exception skipped.", e);
				}
			}
		} finally {
			l.unlock();
		}
	}
	
	@Override
	public void fireCallClientDestroyed(IClientConnection client) {
		Lock l = lock.readLock();
		l.lock();
		try {
			for(ICallObjectsLifetimeListener listener : listeners) {
				try {
					listener.onClientDisconnected(client);
				} catch(Exception e) {
					log.error("Got exception from listener. Exception skipped.", e);
				}
			}
		} finally {
			l.unlock();
		}
	}

}
