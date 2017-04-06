package com.tle.reporting.schema;

import java.io.Serializable;

public class Column implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	private String type;

	public Column(String name, String type)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}
}
