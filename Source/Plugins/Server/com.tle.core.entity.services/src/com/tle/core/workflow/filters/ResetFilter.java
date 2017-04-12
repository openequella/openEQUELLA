/*
 * Created on Aug 4, 2004
 */
package com.tle.core.workflow.filters;

import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author jmaginnis
 */
@Bind
public class ResetFilter extends BaseFilter
{
	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{workflowFactory.reset(), workflowFactory.saveUnlock(false)};
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
