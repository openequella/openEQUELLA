package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'logger' variable in script
 * 
 * @author aholland
 */
public interface LoggingScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "logger"; //$NON-NLS-1$

	/**
	 * Logs a message in the Resource Centre log files. This is logged at the
	 * INFO level and is controlled by the
	 * com.tle.common.scripting.service.ScriptingService logger
	 * 
	 * @param text The text to log
	 */
	void log(String text);
}
