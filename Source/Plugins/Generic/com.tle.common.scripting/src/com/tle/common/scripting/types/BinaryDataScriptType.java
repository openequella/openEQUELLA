package com.tle.common.scripting.types;

/**
 * Used to pass between the various script objects. You should not need to
 * invoke the methods on this interface directly.
 * 
 * @author aholland
 */
public interface BinaryDataScriptType
{
	/**
	 * @return The length of the data (in bytes)
	 */
	long getLength();
}
