/*
 * Created on Mar 8, 2005 For "The Learning Edge"
 */
package com.tle.core.workflow.daily;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.tasks.CheckStepOperation;

@Bind
public class CheckModerationFilter extends BaseFilter
{
	@Inject
	private Provider<EscalateOperation> escalateFactory;
	@Inject
	private Provider<CheckStepOperation> checkStepFactory;

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{escalateFactory.get(), checkStepFactory.get(), workflowFactory.save()};
	}

	@Override
	public String getWhereClause()
	{
		return "moderating = true"; //$NON-NLS-1$
	}
}
