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
public class UserDeletedFilter extends AbstractUserFilter // NOSONAR
{
	@AssistedInject
	private UserDeletedFilter(@Assisted String userID)
	{
		super(userID);
	}

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{workflowFactory.userDeleted(getUserID()), workflowFactory.save()};
	}
}
