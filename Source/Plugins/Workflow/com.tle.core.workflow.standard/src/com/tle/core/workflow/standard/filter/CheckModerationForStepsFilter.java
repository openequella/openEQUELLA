/*
 * Created on Mar 8, 2005 For "The Learning Edge"
 */
package com.tle.core.workflow.standard.filter;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;

/**
 * @author jmaginnis
 */
public class CheckModerationForStepsFilter extends BaseFilter
{
	private final Collection<Long> changedNodes;
	private final boolean forceSave;

	@Inject
	private ItemOperationFactory itemOperationFactory;

	@AssistedInject
	protected CheckModerationForStepsFilter(@Assisted Collection<Long> changedNodes, @Assisted boolean forceSave)
	{
		this.changedNodes = changedNodes;
		this.forceSave = forceSave;
	}

	@Override
	public WorkflowOperation[] createOperations()
	{
		if( forceSave )
		{
			return new WorkflowOperation[]{itemOperationFactory.checkSteps(), itemOperationFactory.forceModify(),
					itemOperationFactory.saveUnlock(false)};
		}
		return new WorkflowOperation[]{itemOperationFactory.checkSteps(), itemOperationFactory.saveUnlock(false)};
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
