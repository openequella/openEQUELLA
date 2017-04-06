package com.tle.upgrade;

import java.util.Date;

public class UpgradeLog
{
	public enum LogStatus
	{
		EXECUTED, SKIPPED, OBSOLETE, ERRORED
	}

	private String migrationId;

	private Date executed;

	private boolean mustExist;
	private boolean canRetry;

	private LogStatus status;
	private String message;
	private String log;
	private String errorMessage;

	public Date getExecuted()
	{
		return executed;
	}

	public void setExecuted(Date executed)
	{
		this.executed = executed;
	}

	public String getMigrationId()
	{
		return migrationId;
	}

	public void setMigrationId(String migrationId)
	{
		this.migrationId = migrationId;
	}

	public boolean isMustExist()
	{
		return mustExist;
	}

	public void setMustExist(boolean mustExist)
	{
		this.mustExist = mustExist;
	}

	public LogStatus getStatus()
	{
		return status;
	}

	public void setStatus(LogStatus status)
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getLog()
	{
		return log;
	}

	public void setLog(String log)
	{
		this.log = log;
	}
}
