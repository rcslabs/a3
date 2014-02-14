package com.rcslabs.rcl.exception;

/**
 * Represents and exception which is recoverable and should be handled at
 * the lowest possible level.
 * The handler should check for the unfreed resources and free them if
 * necessary.
 *
 */
public abstract class RclCheckedException extends Exception {
	private static final long serialVersionUID = -5955746539782522836L;

	public RclCheckedException(String message) {
		super(message);
	}
}
