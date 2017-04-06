package com.tle.common.scripting;


/**
 * Internal use only.
 * 
 * @author aholland
 */
public interface ScriptObject
{
	/**
	 * Used internally. Do not reference this in script!
	 */
	void scriptEnter();

	/**
	 * Used internally. Do not reference this in script!
	 */
	void scriptExit();
}
