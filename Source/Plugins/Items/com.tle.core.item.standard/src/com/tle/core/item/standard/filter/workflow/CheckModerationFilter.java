/*
 * Created on Mar 8, 2005 For "The Learning Edge"
 */
package com.tle.core.item.standard.filter.workflow;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.item.standard.operations.workflow.EscalateOperation;

@Bind
public class CheckModerationFilter extends AbstractStandardOperationFilter
{
	@Inject
	private Provider<EscalateOperation> escalateFactory;
	@Inject
	private ItemOperationFactory itemOperationFactory;

	@Override
	public AbstractStandardWorkflowOperation[] createOperations()
	{
		return new AbstractStandardWorkflowOperation[]{escalateFactory.get(), itemOperationFactory.checkSteps(),
				operationFactory.save()};
	}

	@Override
	public String getWhereClause()
	{
		return "moderating = true"; //$NON-NLS-1$
	}
}
