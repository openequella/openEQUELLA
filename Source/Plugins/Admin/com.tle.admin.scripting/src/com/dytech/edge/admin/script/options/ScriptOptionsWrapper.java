/*
 * Created on Jul 5, 2005
 */
package com.dytech.edge.admin.script.options;

import java.util.List;

import com.tle.common.NameValue;

/**
 * @author nread
 */
public class ScriptOptionsWrapper implements ScriptOptions
{
	private final ScriptOptions parent;

	public ScriptOptionsWrapper(ScriptOptions parent)
	{
		this.parent = parent;
	}

	@Override
	public boolean hasWorkflow()
	{
		return parent.hasWorkflow();
	}

	@Override
	public boolean hasItemStatus()
	{
		return parent.hasItemStatus();
	}

	@Override
	public boolean restrictItemStatusForModeration()
	{
		return parent.restrictItemStatusForModeration();
	}

	@Override
	public List<NameValue> getWorkflowSteps()
	{
		return parent.getWorkflowSteps();
	}

	@Override
	public String getWorkflowStepName(String value)
	{
		return parent.getWorkflowStepName(value);
	}

	@Override
	public boolean hasUserIsModerator()
	{
		return parent.hasUserIsModerator();
	}
}
