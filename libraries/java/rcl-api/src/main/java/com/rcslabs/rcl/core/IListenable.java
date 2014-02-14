package com.rcslabs.rcl.core;

/**
 * Interface for listener-enabled class.
 */
public interface IListenable<T> {
	
	void addListener(T listener);
	
	void removeListener(T listener);
}
