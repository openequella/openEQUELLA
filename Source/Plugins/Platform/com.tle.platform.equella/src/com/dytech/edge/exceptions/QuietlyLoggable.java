package com.dytech.edge.exceptions;

/**
 * Interface for exceptions that can determine whether the should be logged and
 * to what extent.
 * 
 * @author aholland
 */
public interface QuietlyLoggable
{
	/**
	 * You should rarely return true, if ever
	 * 
	 * @return true if this exception should _not_ be reported in the logs
	 */
	boolean isSilent();

	/**
	 * @return true if the full stack should be printed to the logs, otherwise
	 *         false for a single line message
	 */
	boolean isShowStackTrace();

	/**
	 * @return true if it should be logged as a WARN instead of an ERROR
	 * @return
	 */
	boolean isWarnOnly();
}
