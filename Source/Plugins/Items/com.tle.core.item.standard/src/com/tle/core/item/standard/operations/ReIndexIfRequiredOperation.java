package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.item.event.IndexItemBackgroundEvent;

public class ReIndexIfRequiredOperation extends AbstractStandardWorkflowOperation
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
