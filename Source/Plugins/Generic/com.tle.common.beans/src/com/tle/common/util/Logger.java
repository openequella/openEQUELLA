package com.tle.common.util;

/*
 * Interface for anything capable of logging error, warning and debug messages
 * See mainly the LoggingService
 * @author aholland
 */
public interface Logger
{
	boolean isDebugEnabled();

	void debug(String msg);

	void debug(String msg, Throwable t);

	void error(String msg);

	void error(String msg, Throwable t);

	void error(Throwable t);

	void warn(String msg);

	void warn(String msg, Throwable t);

	void info(String msg);

	void info(String msg, Throwable t);
}
