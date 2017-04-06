package com.tle.core.workflow.event;

import com.tle.core.events.listeners.ApplicationListener;

public interface WorkflowChangeListener extends ApplicationListener
{
	void workflowChange(WorkflowChangeEvent event);
}
