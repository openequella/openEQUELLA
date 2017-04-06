/*
 * Created on Mar 8, 2005 For "The Learning Edge"
 */
package com.tle.core.workflow.filters;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.tasks.CheckStepOperation;

/**
 * @author jmaginnis
 */
public class CheckModerationForStepsFilter extends BaseFilter
{
	private final Collection<Long> changedNodes;
	private final boolean forceSave;

	@Inject
	private Provider<CheckStepOperation> checkStepFactory;

	@AssistedInject
	protected CheckModerationForStepsFilter(@Assisted Collection<Long> changedNodes, @Assisted boolean forceSave)
	{
		this.changedNodes = changedNodes;
		this.forceSave = forceSave;
	}

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		if( forceSave )
		{
			return new AbstractWorkflowOperation[]{checkStepFactory.get(), workflowFactory.forceModify(),
					workflowFactory.saveUnlock(false)};
		}
		return new AbstractWorkflowOperation[]{checkStepFactory.get(), workflowFactory.saveUnlock(false)};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("tasks", changedNodes); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public String getWhereClause()
	{
		return "s.node.id in (:tasks)";
	}

	@SuppressWarnings("nls")
	@Override
	public String getJoinClause()
	{
		return "join i.moderation.statuses s";
	}
}
