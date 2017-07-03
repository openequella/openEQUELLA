package com.tle.core.workflow.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.workflow.event.WorkflowChangeEvent;

public interface WorkflowChangeListener extends ApplicationListener
{
	void workflowChange(WorkflowChangeEvent event);
}
