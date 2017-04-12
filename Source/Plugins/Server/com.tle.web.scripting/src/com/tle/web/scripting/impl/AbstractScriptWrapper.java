package com.tle.web.scripting.impl;

import com.tle.common.scripting.ScriptObject;

/**
 * @author aholland
 */
public abstract class AbstractScriptWrapper implements ScriptObject
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2982385813518349536L;

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
