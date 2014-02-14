package com.rcslabs.rcl.core.entity;

import com.rcslabs.rcl.exception.RclException;

/**
 * An error information.
 *
 */
public class ErrorInfo {

	private int errorCode;
	private String errorText;
	private RclException cause;
	
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorText() {
		return errorText;
	}
	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
	public RclException getCause() {
		return cause;
	}
	public void setCause(RclException cause) {
		this.cause = cause;
	}
	public String toString() {
		return "" + errorCode + ": " + errorText + (cause != null ? "(caused by: " + cause + ")" : "");
	}
}
