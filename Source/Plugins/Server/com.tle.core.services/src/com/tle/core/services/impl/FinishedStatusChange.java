package com.tle.core.services.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.tle.core.services.TaskStatusChange;

public class FinishedStatusChange implements TaskStatusChange<FinishedStatusChange>
{
	private static final long serialVersionUID = 1L;

	private String errorMessage;

	public FinishedStatusChange()
	{
		// no error
	}

	public FinishedStatusChange(Throwable t)
	{
		if( t != null )
		{
			StringWriter out = new StringWriter();
			t.printStackTrace(new PrintWriter(out));
			errorMessage = out.toString();
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void merge(FinishedStatusChange newChanges)
	{
		throw new Error("Shouldn't ever be merged");
	}

	@Override
	public void modifyStatus(TaskStatusImpl taskStatus)
	{
		taskStatus.setFinished(true);
		if( errorMessage != null )
		{
			taskStatus.setErrorMessage(errorMessage);
		}
	}

}
