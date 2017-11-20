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

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureItemStatus(ItemStatus.DELETED)
@SecureOnCall(priv = "DELETE_ITEM")
public class RestoreDeletedOperation extends TaskOperation
{
	private String lastRestoredName;

	public String getLastRestoredName()
	{
		return lastRestoredName;
	}

	@Override
	public boolean execute()
	{
		boolean modified = false;

		ModerationStatus moderationStatus = getModerationStatus();
		ItemStatus deletedStatus = moderationStatus.getDeletedStatus();
		if( deletedStatus == null )
		{
			deletedStatus = ItemStatus.LIVE;
		}
		Item item = getItem();
		item.setModerating(moderationStatus.isDeletedModerating());
		setState(deletedStatus);
		modified = true;
		lastRestoredName = CurrentLocale.get(item.getName());
		restoreTasksForItem();
		return modified;
	}
}
