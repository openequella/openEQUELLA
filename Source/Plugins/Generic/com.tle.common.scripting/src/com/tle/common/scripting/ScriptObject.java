package com.tle.common.scripting;

import java.io.Serializable;

/**
 * Internal use only.
 * 
 * @author aholland
 */
public interface ScriptObject extends Serializable
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
