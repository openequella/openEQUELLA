/*
 * Created on Jul 5, 2005
 */
package com.dytech.edge.admin.script.options;

import java.util.List;

import com.tle.common.NameValue;

public interface ScriptOptions
{
	boolean hasWorkflow();

	boolean hasItemStatus();

	boolean restrictItemStatusForModeration();

	List<NameValue> getWorkflowSteps();

	String getWorkflowStepName(String value);

	boolean hasUserIsModerator();
}
