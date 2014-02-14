package com.rcslabs.rcl;

import java.util.Properties;

import gov.nist.core.StackLogger;

public class JainSipEmptyLogger implements StackLogger {

	@Override
	public void disableLogging() {
	}

	@Override
	public void enableLogging() {
	}

	@Override
	public int getLineCount() {
		return 0;
	}

	@Override
	public String getLoggerName() {
		return getClass().getName();
	}

	@Override
	public boolean isLoggingEnabled() {
		return false;
	}

	@Override
	public boolean isLoggingEnabled(int logLevel) {
		return false;
	}

	@Override
	public void logDebug(String message) {
	}

	@Override
	public void logError(String message) {
	}

	@Override
	public void logError(String message, Exception ex) {
	}

	@Override
	public void logException(Throwable ex) {
	}

	@Override
	public void logFatalError(String message) {
	}

	@Override
	public void logInfo(String string) {
	}

	@Override
	public void logStackTrace() {
	}

	@Override
	public void logStackTrace(int traceLevel) {
	}

	@Override
	public void logTrace(String message) {
	}

	@Override
	public void logWarning(String string) {
	}

	@Override
	public void setBuildTimeStamp(String buildTimeStamp) {
	}

	@Override
	public void setStackProperties(Properties stackProperties) {
	}

}
