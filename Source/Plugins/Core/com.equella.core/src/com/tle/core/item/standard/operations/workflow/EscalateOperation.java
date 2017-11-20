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

package com.tle.core.item.standard.operations.workflow;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowItem.AutoAction;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.security.impl.SecureInModeration;

/**
 * @author jmaginnis
 */
@SecureInModeration
@Bind
public class EscalateOperation extends TaskOperation
{
	private static final String PREFIX = PluginServiceImpl.getMyPluginId(EscalateOperation.class) + '.';

	@SuppressWarnings("nls")
	@Override
	public boolean execute()
	{
		ModerationStatus status = getModerationStatus();

		Date datenow = getParams().getDateNow();
		for( WorkflowNodeStatus beanstatus : status.getStatuses() )
		{
			if( beanstatus.getNode().getType() == WorkflowNode.ITEM_TYPE
				&& beanstatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				WorkflowItemStatus bean = (WorkflowItemStatus) beanstatus;
				TaskStatus task = (TaskStatus) getNodeStatus(beanstatus.getNode().getUuid());

				Date itemesc = bean.getDateDue();
				if( itemesc != null && datenow.compareTo(itemesc) >= 0 )
				{
					boolean overdue = bean.isOverdue();
					if( !overdue )
					{
						bean.setOverdue(true);
						
						Collection<String> users = task.getUsersLeft(this);
						if( !users.isEmpty() )
						{
							addModerationNotifications(new ItemTaskId(getItemKey(), task.getId()), users,
								Notification.REASON_OVERDUE, true);
						}
					}
					WorkflowItem node = (WorkflowItem) bean.getNode();
					AutoAction action = node.getAutoAction();
					if( action != AutoAction.NONE )
					{
						Date actionDate = new Date(itemesc.getTime() + TimeUnit.DAYS.toMillis(node.getActionDays()));
						if( datenow.compareTo(actionDate) >= 0 )
						{
							String dateFormatted = new LocalDate(datenow, CurrentTimeZone.get())
								.format(Dates.DATE_ONLY);
							if( action == AutoAction.REJECT )
							{
								params.addOperation(operationFactory.reject(task.getId(),
									CurrentLocale.get(PREFIX + "autorejectmsg", dateFormatted),
									findRejectPoint(node.getParent(), node), null));
							}
							else
							{
								params.addOperation(operationFactory.accept(task.getId(),
									CurrentLocale.get(PREFIX + "autoacceptmsg", dateFormatted), null));
							}
						}
					}

				}
			}
		}
		return false;
	}

	private String findRejectPoint(WorkflowNode parent, WorkflowNode node)
	{
		if( parent.canHaveSiblingRejectPoints() )
		{
			int i = parent.indexOfChild(node) - 1;
			while( i >= 0 )
			{
				WorkflowNode child = parent.getChild(i);
				if( child.isRejectPoint() )
				{
					return child.getUuid();
				}
				i--;
			}
		}

		if( parent.isRejectPoint() )
		{
			return parent.getUuid();
		}

		WorkflowNode newparent = parent.getParent();
		if( newparent != null )
		{
			return findRejectPoint(newparent, parent);
		}
		return null;
	}
}
