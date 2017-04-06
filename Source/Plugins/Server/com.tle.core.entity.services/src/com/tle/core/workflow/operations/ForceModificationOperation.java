package com.tle.core.workflow.operations;

import com.google.inject.assistedinject.AssistedInject;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class ForceModificationOperation extends AbstractWorkflowOperation // NOSONAR
{
	@AssistedInject
	private ForceModificationOperation()
	{
		// nothing
	}

	@Override
	public boolean execute()
	{
		return true;
	}
}
