package com.tle.core.workflow.operations;

import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.events.IndexItemBackgroundEvent;

public class ReIndexIfRequiredOperation extends AbstractWorkflowOperation
{

	@AssistedInject
	public ReIndexIfRequiredOperation()
	{
		super();
	}

	@Override
	public boolean execute()
	{
		if( params.isRequiresReindex() )
		{
			addAfterCommitEvent(new IndexItemBackgroundEvent(params.getItemIdKey(), true));
		}
		return false;
	}

}
