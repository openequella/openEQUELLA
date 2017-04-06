package com.tle.web.wizard.impl;

import java.io.Serializable;

import com.tle.core.workflow.operations.WorkflowOperation;

public interface UnsavedEditOperation extends Serializable
{
	WorkflowOperation getOperation(boolean forInit, boolean forSave);
}
