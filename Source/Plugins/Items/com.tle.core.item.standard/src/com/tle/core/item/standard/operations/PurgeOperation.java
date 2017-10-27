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

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.events.services.EventService;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.UnindexItemEvent;
import com.tle.core.item.event.WaitForItemIndexEvent;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

@SecureItemStatus(ItemStatus.DELETED)
@SecureOnCall(priv = "PURGE_ITEM")
public class PurgeOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private EventService eventService;
	@Inject
	private TLEAclManager aclManager;

	private final boolean wait;

	@AssistedInject
	protected PurgeOperation(@Assisted("wait") boolean wait)
	{
		this.wait = wait;
	}

	@Override
	public boolean execute()
	{
		ItemIdKey id = params.getItemIdKey();
		params.setUpdateSecurity(true);

		eventService.publishApplicationEvent(new ItemDeletedEvent(id));

		Item item = getItem();
		final ItemDefinition collection = item.getItemDefinition();
		aclManager.deleteAllEntityChildren(Node.DYNAMIC_ITEM_METADATA, item.getId());
		itemService.delete(item);

		params.setItemPack(null);

		final ItemId itemId = item.getItemId();
		params.addAfterCommitHook(ItemOperationParams.COMMIT_HOOK_PRIORITY_LOW, new Runnable()
		{
			@Override
			public void run()
			{
				//Item will be dead by here, can't use it directly
				fileSystemService.removeFile(itemFileService.getItemFile(itemId, collection));
			}
		});
		addAfterCommitEvent(new UnindexItemEvent(id, true));
		addAfterCommitEvent(new UnindexItemEvent(id, false));
		if( wait )
		{
			addAfterCommitEvent(new WaitForItemIndexEvent(id));
		}
		return false;
	}

	@Override
	public boolean isDeleteLike()
	{
		return true;
	}
}
