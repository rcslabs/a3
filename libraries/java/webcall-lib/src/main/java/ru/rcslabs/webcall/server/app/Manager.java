package ru.rcslabs.webcall.server.app;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ru.rcslabs.webcall.server.exceptions.WebcallException;

public class Manager<K,V> {
	
	protected Map<K, V> apps = new ConcurrentHashMap<K, V>();
	
	public V get(K id) throws WebcallException {
		V ret = apps.get(id);
		if (ret==null) {
			throw new WebcallException("Managed item "+id+" is not found.");
		}
		return ret;
	}
	
	public boolean has(K id) {
		return apps.containsKey(id);
	}
	
	public void clear() {
		apps.clear();
	}
	
	public Set<K> keys() {
		return apps.keySet();
	}
	
	public void add(K id, V value) {
		apps.put(id, value);
	}
	
	public V remove(K id) {
		return apps.remove(id);
	}
	
}
