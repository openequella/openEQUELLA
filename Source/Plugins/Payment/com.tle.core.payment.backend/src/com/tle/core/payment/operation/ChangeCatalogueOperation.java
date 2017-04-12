package com.tle.core.payment.operation;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemStatus;
import com.tle.core.payment.operation.ChangeCatalogueState.ChangeCatalogueAssignment;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@SecureItemStatus(value = {ItemStatus.PERSONAL}, not = true)
public class ChangeCatalogueOperation extends AbstractWorkflowOperation
{
	private final ChangeCatalogueState state;

	@Inject
	private CatalogueService catalogueService;

	@AssistedInject
	public ChangeCatalogueOperation(@Assisted ChangeCatalogueState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		boolean modified = false;
		for( ChangeCatalogueAssignment assignment : state.getAssignments() )
		{
			if( assignment.isAdd() )
			{
				if( catalogueService.addItemToList(assignment.getCatalogueId(), getItem(), assignment.isBlacklist()) )
				{
					params.setRequiresReindex(true);
					modified = true;
				}
			}
			else
			{
				if( catalogueService.removeItemFromList(assignment.getCatalogueId(), getItem(),
					assignment.isBlacklist()) )
				{
					params.setRequiresReindex(true);
					modified = true;
				}
			}
		}
		return modified;
	}
}
