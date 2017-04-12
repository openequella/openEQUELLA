/*
 * Created on Aug 4, 2004
 */
package com.tle.core.workflow.filters;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SuppressWarnings("nls")
public final class ChangeUserIdFilter extends AbstractUserFilter // NOSONAR
{
	private String toUserId;

	@AssistedInject
	private ChangeUserIdFilter(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId)
	{
		super(fromUserId);
		this.toUserId = toUserId;
	}

	@Override
	public String getWhereClause()
	{
		return super.getWhereClause() + " OR :userId IN ELEMENTS(i.notifications) OR m.rejectedBy = :userId";
	}

	@Override
	public String getJoinClause()
	{
		return " LEFT JOIN i.moderation m";
	}

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{workflowFactory.changeUserId(getUserID(), toUserId),
				workflowFactory.save()};
	}
}
