package ru.rcslabs.webcall.server.exceptions;

public class WebcallMismatchedUidException extends WebcallException {

	private static final long serialVersionUID = -6381095701431790826L;

	public WebcallMismatchedUidException(String message) {
		super(message);
	}

	public WebcallMismatchedUidException(Throwable cause) {
		super(cause);
	}

	public WebcallMismatchedUidException(String message, Throwable cause) {
		super(message, cause);
	}

}
