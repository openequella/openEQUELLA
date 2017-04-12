package com.tle.core.workflow.operations;

import java.io.Serializable;

/**
 * @author aholland Ok, the name is crap. I'll think of a better one later
 */
public interface DuringSaveOperation extends Serializable
{
	String getName();

	WorkflowOperation createPreSaveWorkflowOperation();

	WorkflowOperation createPostSaveWorkflowOperation();
}
