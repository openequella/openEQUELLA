package com.tle.upgrade;

import org.apache.commons.logging.Log;

public class UpgradeResult
{
	private boolean canRetry;
	private boolean retry;
	private String message;
	private StringBuilder workLog = new StringBuilder();
	private Log log;

	public UpgradeResult(Log logger)
	{
		this.log = logger;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void addLogMessage(String message)
	{
		workLog.append(message);
		workLog.append('\n');
	}

	public void info(String message)
	{
		log.info(message);
	}

	public String getWorkLog()
	{
		return workLog.toString();
	}

	public boolean isRetry()
	{
		return retry;
	}

	public void setRetry(boolean retry)
	{
		this.retry = retry;
	}

}
