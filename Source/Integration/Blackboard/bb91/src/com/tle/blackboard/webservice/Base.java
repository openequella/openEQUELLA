package com.tle.blackboard.webservice;

public class Base
{
	private String id;
	private String name;
	private long createdDate;
	private long modifiedDate;

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

	public long getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(long createdDate)
	{
		this.createdDate = createdDate;
	}

	public long getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(long modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}
}
