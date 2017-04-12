/*
 * Created on Jul 5, 2005
 */
package com.dytech.edge.admin.script.options;

import java.util.List;

import com.tle.common.NameValue;

public class DefaultScriptOptions implements ScriptOptions
{
	@Override
	public boolean hasWorkflow()
	{
		return false;
	}

	@Override
	public boolean hasItemStatus()
	{
		return true;
	}

	@Override
	public boolean restrictItemStatusForModeration()
	{
		return false;
	}

	@Override
	public boolean hasUserIsModerator()
	{
		return true;
	}

	@Override
	public List<NameValue> getWorkflowSteps()
	{
		throw new RuntimeException("Operation not supported");
	}

	@Override
	public String getWorkflowStepName(String value)
	{
		throw new RuntimeException("Operation not supported");
	}
}
