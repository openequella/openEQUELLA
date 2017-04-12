/**
 * 
 */
package com.tle.admin.taxonomy.tool.internal;

class DataEntry
{
	private final String key;
	private final String displayName;

	private String value;

	public DataEntry(String key)
	{
		this(key, key);
	}

	public DataEntry(String key, String displayName)
	{
		this.key = key;
		this.displayName = displayName;
	}

	public String getKey()
	{
		return key;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}