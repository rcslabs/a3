package ru.rcslabs.webcall.server.exceptions;

public class WebcallException extends RuntimeException {

	private static final long serialVersionUID = -3132040299956611878L;

	private String id;
	private int code;

	public WebcallException(String id, int code, String message) {
		super(message);
		this.id = id;
		this.code = code;
	}

	public WebcallException(String message) {
		super(message);
	}

	public WebcallException(Throwable cause) {
		super(cause);
	}

	public WebcallException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
