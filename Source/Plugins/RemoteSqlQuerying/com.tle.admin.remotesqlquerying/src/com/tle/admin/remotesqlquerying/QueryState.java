/**
 * 
 */
package com.tle.admin.remotesqlquerying;

public class QueryState
{
	private final String key;
	private final String name;
	private final String description;

	private String sql;

	public QueryState(String key, String name, String description)
	{
		this.key = key;
		this.name = name;
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	public String getKey()
	{
		return key;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	@Override
	public String toString()
	{
		return name;
	}
}