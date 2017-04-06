package com.tle.web.api.search.bean;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

public class SearchDefinitionBean extends AbstractExtendableBean
{
	private String name;
	private String id;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
