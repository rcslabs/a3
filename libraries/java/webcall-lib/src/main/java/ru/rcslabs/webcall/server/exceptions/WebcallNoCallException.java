package ru.rcslabs.webcall.server.exceptions;

public class WebcallNoCallException extends WebcallException {

	private static final long serialVersionUID = 1612903235412399448L;

	public WebcallNoCallException(String message) {
		super(message);
	}

	public WebcallNoCallException(Throwable cause) {
		super(cause);
	}

	public WebcallNoCallException(String message, Throwable cause) {
		super(message, cause);
	}

}
