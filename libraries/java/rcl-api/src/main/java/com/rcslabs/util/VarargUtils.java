package com.rcslabs.util;

import java.util.Collection;

public class VarargUtils {
	private VarargUtils() {}
	
	@SuppressWarnings("unchecked")
	public static <T> T findArg(Object[] args, Class<T> cls) {
		for(Object arg : args) {
			if(cls.isInstance(arg)) return (T)arg;
			
			if(arg instanceof Collection) {
				T ret = findArg(((Collection<?>)arg).toArray(), cls);
				if(ret != null) return ret;
			}
		}
		return null;
	}	
}
