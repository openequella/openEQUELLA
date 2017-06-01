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

package com.tle.core.workflow.operations;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.services.LockingService;
import com.tle.core.services.StagingService;

@SuppressWarnings("nls")
public class CancelEditOperation extends AbstractWorkflowOperation
{
	@Inject
	private LockingService lockingService;
	@Inject
	private StagingService stagingService;

	private final String stagingId;
	private final boolean unlock;

	@AssistedInject
	protected CancelEditOperation(@Assisted("stagingId") @Nullable String stagingId, @Assisted("unlock") boolean unlock)
	{
		this.stagingId = stagingId;
		this.unlock = unlock;
	}

	@Override
	public boolean execute()
	{
		final ItemPack<Item> itemPack = getItemPack();
		if( itemPack != null )
		{
			itemPack.setStagingID(stagingId);
		}
		itemService.executeExtensionOperationsNow(params, "preCancel");

		if( stagingId != null )
		{
			stagingService.removeStagingArea(new StagingFile(stagingId), true);
		}

		final Item item = getItem();
		if( item != null && unlock )
		{
			lockingService.unlockItem(item, false);
		}
		return false;
	}
}
