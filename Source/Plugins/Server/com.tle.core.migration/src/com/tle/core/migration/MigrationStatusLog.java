package com.tle.core.migration;

import java.io.Serializable;

public class MigrationStatusLog implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum LogType
	{
		MESSAGE, SQL, WARNING
	}

	private final LogType type;
	private boolean failed;
	private String key;
	private Object[] values;

	public MigrationStatusLog(String sql, boolean failed)
	{
		this.type = LogType.SQL;
		this.failed = failed;
		this.values = new String[]{sql};
	}

	public MigrationStatusLog(String key, Object... values)
	{
		this(LogType.MESSAGE, key, values);
	}

	public MigrationStatusLog(LogType type, String key, Object... values)
	{
		this.key = key;
		this.values = values;
		this.type = type;
	}

	public LogType getType()
	{
		return type;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public Object[] getValues()
	{
		return values;
	}

	public void setValues(Object[] values)
	{
		this.values = values;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public void setFailed(boolean failed)
	{
		this.failed = failed;
	}
}
