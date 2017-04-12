package com.tle.core.migration.log;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
public class MigrationLog
{
	public enum LogStatus
	{
		EXECUTED, SKIPPED, OBSOLETE, ERRORED
	}

	@Id
	@Column(length = 255)
	private String migrationId;

	@Column(nullable = false)
	private Date executed;

	private boolean mustExist;
	private boolean canRetry;

	@Column(nullable = false)
	private LogStatus status;
	@Lob
	private String message;
	@Lob
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
}
