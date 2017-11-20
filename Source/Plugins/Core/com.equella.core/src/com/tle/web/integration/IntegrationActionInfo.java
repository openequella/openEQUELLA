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

package com.tle.web.integration;

import java.util.HashMap;
import java.util.Map;

/*
 * @author aholland
 */
public class IntegrationActionInfo
{
	protected String name;
	protected String path;
	protected String selectable;
	protected Map<String, Object> optionMap = new HashMap<String, Object>();

	public void initFromOther(IntegrationActionInfo basedOn)
	{
		this.name = basedOn.name;
		this.path = basedOn.path;
		this.selectable = basedOn.selectable;
		if( basedOn.optionMap != null )
		{
			this.optionMap.putAll(basedOn.optionMap);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getSelectable()
	{
		return selectable;
	}

	public void setSelectable(String selectable)
	{
		this.selectable = selectable;
	}

	public Map<String, Object> getOptionMap()
	{
		return optionMap;
	}

	public void setOptionMap(Map<String, Object> optionMap)
	{
		this.optionMap = optionMap;
	}

}
