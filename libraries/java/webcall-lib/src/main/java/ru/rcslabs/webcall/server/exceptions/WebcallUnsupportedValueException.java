package ru.rcslabs.webcall.server.exceptions;

public class WebcallUnsupportedValueException extends WebcallException {

	private static final long serialVersionUID = -885265244051337267L;

	public WebcallUnsupportedValueException(String message) {
		super(message);
	}

	public WebcallUnsupportedValueException(Throwable cause) {
		super(cause);
	}

	public WebcallUnsupportedValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
