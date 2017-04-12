package com.tle.web.manualdatafixes;

import com.tle.core.services.TaskStatus;
import com.tle.web.sections.render.Label;

public abstract class ManualDataFixModel
{
	protected boolean checkedStatus;
	protected boolean inProgress;
	protected TaskStatus taskStatus;
	protected Label taskLabel;

	public boolean isInProgress()
	{
		return inProgress;
	}

	public void setInProgress(boolean inProgress)
	{
		this.inProgress = inProgress;
	}

	public abstract TaskStatus getTaskStatus();

	public Label getTaskLabel()
	{
		return taskLabel;
	}

	public void setTaskLabel(Label taskLabel)
	{
		this.taskLabel = taskLabel;
	}

	public boolean isCheckedStatus()
	{
		return checkedStatus;
	}

	public void setCheckedStatus(boolean checkedStatus)
	{
		this.checkedStatus = checkedStatus;
	}
}
