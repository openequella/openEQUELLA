/*
 * Created on Aug 4, 2004
 */
package com.tle.core.item.standard.filter.workflow;

import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;

/**
 * @author jmaginnis
 */
@Bind
public class ResetFilter extends AbstractStandardOperationFilter
{
	@Override
	public WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{operationFactory.reset(), operationFactory.saveUnlock(false)};
	}

	@SuppressWarnings("nls")
	@Override
	public String getWhereClause()
	{
		return "m.needsReset = true";
	}

	@Override
	public String getJoinClause()
	{
		return "join i.moderation m"; //$NON-NLS-1$
	}
}
