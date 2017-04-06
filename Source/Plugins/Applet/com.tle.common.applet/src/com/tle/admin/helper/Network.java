package com.tle.admin.helper;

/**
 * @author Nicholas Read
 */
public class Network
{
	private String name;
	private String min;
	private String max;

	public Network()
	{
		super();
	}

	public Network(String name, String min, String max)
	{
		this.name = name;
		this.min = min;
		this.max = max;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public String getMax()
	{
		return max;
	}

	public String getMin()
	{
		return min;
	}

	public String getName()
	{
		return name;
	}

	public void setMax(String max)
	{
		this.max = max;
	}

	public void setMin(String min)
	{
		this.min = min;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
