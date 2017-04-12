package com.tle.core.portal.service;

public class PortletSearch
{
	private String owner;
	private String query;
	private String type;
	private Boolean onlyInstWide;

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setOnlyInstWide(Boolean onlyInstWide)
	{
		this.onlyInstWide = onlyInstWide;
	}

	public Boolean isOnlyInstWide()
	{
		return onlyInstWide;
	}

}
