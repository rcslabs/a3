package com.rcslabs.rcl.exception;

/**
 * The specified service is not enabled or is not implemented.
 *
 */
public class ServiceNotEnabledException extends RclCheckedException {
	private static final long serialVersionUID = 7494210896856321722L;

	public ServiceNotEnabledException(String message) {
		super(message);
	}
}
