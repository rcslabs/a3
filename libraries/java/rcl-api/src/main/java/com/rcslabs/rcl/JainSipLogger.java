package com.rcslabs.rcl;

import gov.nist.core.StackLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal class that is provided to SipFactory as a logger for SipStack.
 * 
 * @author Viktor Kazakov
 *
 */
public class JainSipLogger implements StackLogger {
	private static Logger log = LoggerFactory.getLogger(JainSipLogger.class);

	public void disableLogging() {
		//do nothing
	}

	public void enableLogging() {
		//do nothing
	}

	public int getLineCount() {
		return 0;
	}

	public String getLoggerName() {
		return log.getName();
	}

	public boolean isLoggingEnabled() {
		return log.isInfoEnabled();
	}

	public boolean isLoggingEnabled(int logLevel) {
		switch(logLevel) {
		case TRACE_INFO:
			return log.isInfoEnabled();
		case TRACE_DEBUG: 
			return log.isDebugEnabled();
    	case TRACE_ERROR: 
    		return log.isErrorEnabled();
    	case TRACE_WARN: 
    		return log.isWarnEnabled();
    	case TRACE_TRACE: 
    		return log.isTraceEnabled();
    	case TRACE_FATAL: 
    		return false;
		}
		return false;
	}

	public void logDebug(String message) {
		log.debug(message);
	}

	public void logError(String message) {
		log.error(message);
	}

	public void logError(String message, Exception ex) {
		log.error(message, ex);
	}

	public void logException(Throwable ex) {
		log.error("", ex);
	}

	public void logFatalError(String message) {
		log.error(message);
	}

	public void logInfo(String string) {
		log.info(string);
	}

	public void logStackTrace() {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StackTraceElement[] ste = new Exception().getStackTrace();
        // Skip the log writer frame and log all the other stack frames.
        for (int i = 1; i < ste.length; i++) {
            String callFrame = "[" + ste[i].getFileName() + ":"
                    + ste[i].getLineNumber() + "]";
            pw.print(callFrame);
        }
        pw.close();
        String stackTrace = sw.getBuffer().toString();
        log.trace(stackTrace);
	}

	public void logStackTrace(int traceLevel) {
		this.logStackTrace();
	}

	public void logTrace(String message) {
		log.trace(message);
	}

	public void logWarning(String string) {
		log.warn(string);
	}

	public void setBuildTimeStamp(String buildTimeStamp) {
		log.info("Build timestamp: " + buildTimeStamp);
	}

	public void setStackProperties(Properties stackProperties) {
		//do nothing, legacy
	}

}
