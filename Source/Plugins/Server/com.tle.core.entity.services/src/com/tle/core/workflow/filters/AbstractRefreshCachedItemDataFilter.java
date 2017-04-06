/*
 * Created on Aug 4, 2004
 */
package com.tle.core.workflow.filters;

import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author Nicholas Read
 */
public abstract class AbstractRefreshCachedItemDataFilter extends BaseFilter
{
	@Override
	protected AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{workflowFactory.forceModify(), workflowFactory.saveNoSaveScript(true)};
	}
}
