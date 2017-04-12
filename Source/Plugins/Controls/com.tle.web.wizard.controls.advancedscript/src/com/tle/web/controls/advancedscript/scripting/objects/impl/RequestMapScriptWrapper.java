package com.tle.web.controls.advancedscript.scripting.objects.impl;

import java.util.Map;

import com.tle.web.controls.advancedscript.scripting.objects.RequestMapScriptObject;

/**
 * @author aholland
 */
public class RequestMapScriptWrapper implements RequestMapScriptObject
{
	private static final long serialVersionUID = 1L;

	final String prefix;
	final Map<Object, Object> requestMap;

	public RequestMapScriptWrapper(String prefix, Map<Object, Object> requestMap)
	{
		this.prefix = prefix;
		this.requestMap = requestMap;
	}

	@Override
	public String get(String key)
	{
		String[] values = getList(key);
		if( values != null && values.length > 0 )
		{
			return values[0];
		}
		return null;
	}

	@Override
	public String[] getList(String key)
	{
		return (String[]) requestMap.get(prefix + key);
	}

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