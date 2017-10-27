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
import java.util.Iterator;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;

@Bind
@Singleton
public class RemoveOrphanedWorkflowStatus implements PostReadMigrator<ItemConverterInfo>
{
	@Override
	public void migrate(ItemConverterInfo obj) throws IOException
	{
		ModerationStatus moderation = obj.getItem().getModeration();
		if( moderation != null )
		{
			Set<WorkflowNodeStatus> statuses = moderation.getStatuses();
			if( !Check.isEmpty(statuses) )
			{
				Iterator<WorkflowNodeStatus> iter = statuses.iterator();
				while( iter.hasNext() )
				{
					WorkflowNodeStatus status = iter.next();
					if( status.getNode() == null )
					{
						iter.remove();
					}
				}
			}
		}
	}
}