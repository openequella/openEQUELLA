/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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