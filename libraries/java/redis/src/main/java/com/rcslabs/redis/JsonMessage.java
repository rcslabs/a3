package com.rcslabs.redis;

import java.util.HashMap;
import java.util.Map;

public class JsonMessage implements IMessage {

    protected Map<String, Object> data;
	
	public Map<String, Object> getData() {
		return data;
	}

	public JsonMessage() {
		super();
		this.data = new HashMap<String, Object>();
	}

	@Override	
	public void set(String key, Object value){
		data.put(key, value);
	}
	
	@Override	
	public Object get(String key){ 
		return data.get(key); 
	}

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
	public void delete(String key) {
		data.remove(key);
	}
}
