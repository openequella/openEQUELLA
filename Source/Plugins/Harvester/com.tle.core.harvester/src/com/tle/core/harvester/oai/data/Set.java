/*
 * Created on Apr 12, 2005
 */

package com.tle.core.harvester.oai.data;

/**
 * 
 */
public class Set
{
	private String spec;
	private String name;
	private Object description;

	public Object getDescription()
	{
		return description;
	}

	public void setDescription(Object description)
	{
		this.description = description;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSpec()
	{
		return spec;
	}

	public void setSpec(String spec)
	{
		this.spec = spec;
	}
}
