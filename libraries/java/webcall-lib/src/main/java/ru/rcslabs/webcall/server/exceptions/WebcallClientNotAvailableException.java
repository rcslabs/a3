package ru.rcslabs.webcall.server.exceptions;

public class WebcallClientNotAvailableException extends WebcallException {

	private static final long serialVersionUID = -2857461640247206702L;

	public WebcallClientNotAvailableException(String message) {
		super(message);
	}

	public WebcallClientNotAvailableException(Throwable cause) {
		super(cause);
	}

	public WebcallClientNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
