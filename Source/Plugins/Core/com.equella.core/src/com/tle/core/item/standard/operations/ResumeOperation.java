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
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = "SUSPEND_ITEM")
@SecureItemStatus(ItemStatus.SUSPENDED)
public class ResumeOperation extends TaskOperation
{
	protected ResumeOperation()
	{
		// please use guice
	}

	@Override
	public boolean execute()
	{
		ModerationStatus moderationStatus = getModerationStatus();
		Item item = getItem();
		ItemStatus resumeStatus = moderationStatus.getResumeStatus();
		if( resumeStatus == null )
		{
			resumeStatus = ItemStatus.LIVE;
		}
		item.setModerating(moderationStatus.isResumeModerating());
		setState(resumeStatus);
		restoreTasksForItem();
		return true;
	}
}
