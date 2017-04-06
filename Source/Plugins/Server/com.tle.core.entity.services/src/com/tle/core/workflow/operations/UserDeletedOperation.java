/*
 * Created on Aug 4, 2004
 */
package com.tle.core.workflow.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class UserDeletedOperation extends AbstractWorkflowOperation // NOSONAR
{
	private final String user;

	@AssistedInject
	private UserDeletedOperation(@Assisted String user)
	{
		this.user = user;
	}

	@Override
	public boolean execute()
	{
		if( isOwner(user) )
		{
			setOwner(""); //$NON-NLS-1$
		}
		getItem().getCollaborators().remove(user);
		return true;
	}
}
