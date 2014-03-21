package com.rcslabs.redis;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class JsonMessageDeserializer implements JsonDeserializer<IMessage>
{
	public IMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		return handleObject(json.getAsJsonObject(), context);
	}

	// FIXME: invalid deserialization int 777 to float like a 777.0
	private IMessage handleObject(JsonObject json, JsonDeserializationContext context)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (Map.Entry<String, JsonElement> entry : json.entrySet()){ 				
			Object value = context.deserialize(entry.getValue(), Object.class);
			map.put(entry.getKey(), value);
		}

        IMessage m = new JsonMessage();
		for(String key : map.keySet()){
			m.set(key, map.get(key));
		}
		
		return m;
	}
}