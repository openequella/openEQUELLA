package com.tle.core.migration;

import java.io.Serializable;
import java.util.List;

import com.tle.common.i18n.InternalI18NString;

public class MigrationErrorReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private InternalI18NString name;
	private String message;
	private String error;
	private boolean canRetry;

	private String databaseType;
	private List<MigrationStatusLog> log;

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public String getDatabaseType()
	{
		return databaseType;
	}

	public void setDatabaseType(String databaseType)
	{
		this.databaseType = databaseType;
	}

	public InternalI18NString getName()
	{
		return name;
	}

	public void setName(InternalI18NString name)
	{
		this.name = name;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public List<MigrationStatusLog> getLog()
	{
		return log;
	}

	public void setLog(List<MigrationStatusLog> log)
	{
		this.log = log;
	}
}
