package com.tle.core.workflow.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.workflow.extension.WorkflowNodesSaveExtension;

/**
 * @author Aaron
 *
 */
public class WorkflowModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(WorkflowNodesSaveExtension.class, "workflowNodesSave", "bean");
	}
}
