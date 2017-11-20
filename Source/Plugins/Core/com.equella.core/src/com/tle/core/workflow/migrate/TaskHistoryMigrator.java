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

package com.tle.core.workflow.migrate;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.beans.TaskHistory;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;

@Bind
@Singleton
public class TaskHistoryMigrator implements PostReadMigrator<ItemConverterInfo>
{
	@Override
	public void migrate(ItemConverterInfo info) throws IOException
	{
		Item item = info.getItem();

		if( item.getStatus() == ItemStatus.MODERATING )
		{
			item.setModerating(true);
		}
		ModerationStatus moderation = item.getModeration();
		if( moderation == null || !item.isModerating() )
		{
			return;
		}
		Set<WorkflowNodeStatus> statuses = moderation.getStatuses();
		List<TaskHistory> taskHistories = Lists.newArrayList();

		for( WorkflowNodeStatus wn : statuses )
		{
			if( wn instanceof WorkflowItemStatus )
			{
				WorkflowItemStatus taskStatus = (WorkflowItemStatus) wn;
				Date started = taskStatus.getStarted();
				WorkflowItem node = (WorkflowItem) wn.getNode();
				if( started == null )
				{
					Date dateDue = taskStatus.getDateDue();
					if( dateDue != null )
					{
						started = new Date(dateDue.getTime() - TimeUnit.DAYS.toMillis(node.getEscalationdays()));
					}
					else
					{
						started = new Date();
					}
					taskStatus.setStarted(started);
				}
				if( wn.getStatus() == 'i' )
				{
					TaskHistory th = new TaskHistory();
					th.setItem(item);
					th.setTask(node);
					th.setEntryDate(started);
					th.setExitDate(null);
					taskHistories.add(th);
				}
			}
		}

		if( !Check.isEmpty(taskHistories) )
		{
			info.setItemAttribute(TaskHistoryMigrator.class, taskHistories);
		}
	}
}
