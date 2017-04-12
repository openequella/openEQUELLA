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
