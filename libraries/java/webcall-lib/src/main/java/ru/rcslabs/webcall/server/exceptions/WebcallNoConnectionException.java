package ru.rcslabs.webcall.server.exceptions;

public class WebcallNoConnectionException extends WebcallException {

	private static final long serialVersionUID = 928140720584985631L;

	public WebcallNoConnectionException(String message) {
		super(message);
	}

	public WebcallNoConnectionException(Throwable cause) {
		super(cause);
	}

	public WebcallNoConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
