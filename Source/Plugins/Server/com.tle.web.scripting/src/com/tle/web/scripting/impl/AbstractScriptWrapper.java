package com.tle.web.scripting.impl;

import com.tle.common.scripting.ScriptObject;

/**
 * @author aholland
 */
public abstract class AbstractScriptWrapper implements ScriptObject
{
	@Override
	public void scriptEnter()
	{
		// Nothing by default
	}

	@Override
	public void scriptExit()
	{
		// Nothing by default
	}
}
