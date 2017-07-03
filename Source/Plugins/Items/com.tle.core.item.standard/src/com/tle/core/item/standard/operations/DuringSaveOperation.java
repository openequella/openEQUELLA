package com.tle.core.item.standard.operations;

import java.io.Serializable;

import com.tle.core.item.operations.WorkflowOperation;

/**
 * @author aholland Ok, the name is crap. I'll think of a better one later
 */
public interface DuringSaveOperation extends Serializable
{
	String getName();

	WorkflowOperation createPreSaveWorkflowOperation();

	WorkflowOperation createPostSaveWorkflowOperation();
}
