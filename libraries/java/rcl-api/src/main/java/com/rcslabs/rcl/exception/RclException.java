package com.rcslabs.rcl.exception;

/**
 * Represents a general UNRECOVERABLE exception in the current unit of work.
 * This exception is unchecked and should be thrown only after all the
 * underlying resources in the current unit of work are released.
 *
 */
public class RclException extends RuntimeException {
	private static final long serialVersionUID = 2807396132471357795L;

	public RclException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RclException(Throwable cause) {
		super(cause);
	}

	public RclException(String message) {
		super(message);
	}
	
}
