package com.rcslabs.redis;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonMessageSerializer implements JsonSerializer<IMessage>  {

	@Override
	public JsonElement serialize(IMessage src, Type typeOfSrc, JsonSerializationContext context) {
        
		JsonObject obj = new JsonObject();

        Iterator it = src.getData().entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = (Map.Entry)it.next();
            String key = pairs.getKey();
			Object val = pairs.getValue();

			if(null == val){
				obj.add(key, null);			
			}else if(val instanceof String){
				obj.addProperty(key, (String) val);
			}else if(val instanceof Number){
				obj.addProperty(key, (Number) val);				
			}else if(val instanceof Boolean){
				obj.addProperty(key, (Boolean) val);
			}else if(val instanceof Map<?, ?>){
				obj.add(key, context.serialize(val, Map.class));
			}else if(val instanceof List<?>){
				obj.add(key, context.serialize(val, List.class));
			}else{
				throw new JsonSyntaxException("Unknown type " + val.getClass().getName() + " at key="+key);
			}
		}        
        
        return obj;
	}
}
