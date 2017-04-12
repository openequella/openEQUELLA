package com.tle.common.scripting.types;

/**
 * @author Aaron
 */
public interface ExecutionResultScriptType
{
	/**
	 * @return The return code as returned by the external program
	 */
	int getCode();

	/**
	 * @return The error text emitted by the external program
	 */
	String getErrorOutput();

	/**
	 * @return The standard text emitted by the external program
	 */
	String getStandardOutput();
}
