/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
