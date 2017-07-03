package com.tle.core.item.standard.operations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.operations.WorkflowOperation;

/**
 * Completely unsecured, calls either EditNewItemMetadata or
 * EditExistingItemMetadata which <em>are</em> secured
 * 
 * @author aholland
 */
public class EditMetadataOperation extends AbstractStandardWorkflowOperation
{
	protected final ItemPack<Item> newPack;

	@Inject
	private Provider<EditNewItemMetadataOperation> newOpFactory;
	@Inject
	private Provider<EditExistingItemMetadataOperation> existingOpFactory;

	private ItemStatus initialStatus = ItemStatus.DRAFT;

	public void setInitialStatus(ItemStatus initialStatus)
	{
		this.initialStatus = initialStatus;
	}

	@AssistedInject
	protected EditMetadataOperation(@Assisted ItemPack<Item> pack)
	{
		this.newPack = pack;
	}

	@Override
	public boolean execute()
	{
		List<WorkflowOperation> metaList = new ArrayList<WorkflowOperation>();
		AbstractEditMetadataOperation meta;
		if( newPack.getItem().isNewItem() )
		{
			EditNewItemMetadataOperation newOp = newOpFactory.get();
			newOp.setInitialStatus(initialStatus);
			meta = newOp;
		}
		else
		{
			meta = existingOpFactory.get();
		}
		meta.setItemPack(newPack);
		metaList.add(meta);
		itemService.executeOperationsNow(params, metaList);

		return false;
	}
}
