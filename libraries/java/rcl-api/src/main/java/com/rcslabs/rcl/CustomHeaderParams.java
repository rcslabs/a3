package com.rcslabs.rcl;


public abstract class CustomHeaderParams implements ICustomHeaderParams {

	@Override
	public ICustomHeaderParams clone() {
		try {
			return (ICustomHeaderParams)super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
