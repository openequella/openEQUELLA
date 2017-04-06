package com.tle.common.security;

import java.io.Serializable;

public class WorkflowTaskDynamicTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long itemId;

	public WorkflowTaskDynamicTarget(long itemId)
	{
		this.itemId = itemId;
	}

	public String getTarget()
	{
		return SecurityConstants.TARGET_WORKFLOW_DYNAMIC_TASK + ':' + itemId + ':';
	}

}
