/*
 * Created on Aug 4, 2004
 */
package com.tle.core.item.standard.filter;

import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

/**
 * @author Nicholas Read
 */
public abstract class AbstractRefreshCachedItemDataFilter extends AbstractStandardOperationFilter
{
	@Override
	protected AbstractStandardWorkflowOperation[] createOperations()
	{
		return new AbstractStandardWorkflowOperation[]{operationFactory.forceModify(),
				operationFactory.saveNoSaveScript(true)};
	}
}
