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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;

@Bind
@Singleton
public class AddNotificationSchemaItemXML extends AbstractItemXmlMigrator implements PostReadMigrator<ItemConverterInfo>
{
	@SuppressWarnings("nls")
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		return xml.deleteNode("moderation/overallEscalation");
	}

	@Override
	public void migrate(ItemConverterInfo obj) throws IOException
	{
		Item item = obj.getItem();
		ModerationStatus moderation = item.getModeration();
		if( item.getStatus() == ItemStatus.REJECTED )
		{
			List<HistoryEvent> history = item.getHistory();
			ListIterator<HistoryEvent> iter = history.listIterator();
			while( iter.hasPrevious() )
			{
				HistoryEvent event = iter.previous();
				if( event.getType() == Type.rejected )
				{
					moderation.setRejectedBy(event.getUser());
					moderation.setRejectedMessage(event.getComment());
					moderation.setRejectedStep(event.getStep());
				}
			}
		}
		else if( item.isModerating() )
		{
			List<HistoryEvent> history = item.getHistory();
			ListIterator<HistoryEvent> iter = history.listIterator();
			Map<String, WorkflowNode> nodeMap = item.getItemDefinition().getWorkflow().getAllNodesAsMap();
			boolean finished = false;
			while( iter.hasPrevious() && !finished )
			{
				HistoryEvent event = iter.previous();
				switch( event.getType() )
				{
					case comment:
					case rejected:
						Set<WorkflowNodeStatus> statuses = moderation.getStatuses();
						WorkflowNodeStatus status = null;
						for( WorkflowNodeStatus workflowNodeStatus : statuses )
						{
							if( workflowNodeStatus.getStatus() == 'i'
								&& workflowNodeStatus.getNode().getUuid().equals(event.getStep()) )
							{
								status = workflowNodeStatus;
								break;
							}
						}
						if( status == null )
						{
							status = new WorkflowItemStatus();
							status.setStatus(WorkflowNodeStatus.ARCHIVED);
							status.setNode(nodeMap.get(event.getStep()));
							statuses.add(status);
						}
						WorkflowMessage message = new WorkflowMessage();
						message.setUser(event.getUser());
						message.setNode(status);
						message.setType(event.getType() == Type.rejected ? WorkflowMessage.TYPE_REJECT
							: WorkflowMessage.TYPE_COMMENT);
						message.setMessage(event.getComment());
						status.getComments().add(message);
						break;

					case resetworkflow:
						finished = true;
						break;

					default:
						break;
				}
			}

		}
	}
}
