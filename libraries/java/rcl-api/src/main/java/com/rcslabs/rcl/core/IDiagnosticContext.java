package com.rcslabs.rcl.core;

/**
 * A logging context with keeps the key-value pairs.
 *
 */
public interface IDiagnosticContext {
	Object get(Object key);
	void set(Object key, Object value);
	void toStringBuilder(StringBuilder sb);
	String toString();
}
