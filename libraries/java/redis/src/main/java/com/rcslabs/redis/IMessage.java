package com.rcslabs.redis;



import java.util.Map;

public interface IMessage {

    Map<String, Object> getData();

	void set(String key, Object value);
	
	Object get(String key);

    boolean has(String key);

	void delete(String key);
}
