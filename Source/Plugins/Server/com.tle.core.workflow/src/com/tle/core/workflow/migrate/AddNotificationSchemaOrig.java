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

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowItem.Priority;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddNotificationSchemaOrig extends AbstractHibernateSchemaMigration
{
	private static final String STATUS_TABLE = "workflow_node_status";
	private static final String TABLE = "workflow_node";
	private static final String MODERATION_TABLE = "moderation_status";
	private static String keyPrefix = PluginServiceImpl.getMyPluginId(AddNotificationSchemaOrig.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migratenote.title"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		ScrollableResults scroll = session.createQuery("from WorkflowNode where type = 't'").scroll();
		while( scroll.next() )
		{
			FakeWorkflowItem item = (FakeWorkflowItem) scroll.get(0);
			item.priority = Priority.NORMAL.intValue();
			item.moveliveAccept = false;
			item.actionDays = 0;
			if( item.escalate && item.escalationdays == 0 )
			{
				item.escalate = false;
			}
			session.save(item);
			session.flush();
			session.clear();
			result.incrementStatus();
		}
		session.createQuery("update WorkflowNodeStatus set overdue = false where acttype = 'task'").executeUpdate();
		scroll = session.createQuery("from Item where moderating = true or status = 'REJECTED'").scroll();
		while( scroll.next() )
		{
			FakeItem item = (FakeItem) scroll.get(0);
			if( item.status.equals("REJECTED") )
			{
				List<FakeHistoryEvent> history = item.history;
				for( int i = history.size() - 1; i >= 0; i-- )
				{
					FakeHistoryEvent event = history.get(i);
					if( event.type.equals("rejected") )
					{
						item.moderation.rejectedMessage = event.comment;
						item.moderation.rejectedBy = event.user;
						item.moderation.rejectedStep = event.step;
						session.save(item.moderation);
					}
				}
			}
			else
			{
				item.dateForIndex = new Date();
				List<FakeHistoryEvent> history = item.history;
				for( int i = history.size() - 1; i >= 0; i-- )
				{
					FakeHistoryEvent event = history.get(i);
					String eventType = event.type;
					if( eventType.equals("resetworkflow") )
					{
						break;
					}
					if( eventType.equals("rejected") || eventType.equals("comment") )
					{
						Query q = session
							.createQuery("from WorkflowNodeStatus ns join ns.wnode as n where n.workflowId = ? and n.uuid = ? and ns.status = 'i'");
						q.setLong(0, item.itemDefinition.workflowId);
						q.setString(1, event.step);
						List<Object[]> nodeStatuses = q.list();
						FakeWorkflowItemStatus itemStatus;
						if( nodeStatuses.isEmpty() )
						{
							Query q2 = session.createQuery("from WorkflowNode where workflowId = ? and uuid = ?");
							q2.setLong(0, item.itemDefinition.workflowId);
							q2.setString(1, event.step);
							FakeWorkflowItem node = (FakeWorkflowItem) q2.uniqueResult();
							itemStatus = new FakeWorkflowItemStatus();
							itemStatus.acttype = "task";
							itemStatus.overdue = false;
							itemStatus.modStatus = item.moderation;
							itemStatus.wnode = node;
							itemStatus.status = 'a';
							session.save(itemStatus);
						}
						else
						{
							itemStatus = (FakeWorkflowItemStatus) nodeStatuses.get(0)[0];
						}

						FakeWorkflowMessage message = new FakeWorkflowMessage();
						message.type = eventType.equals("rejected") ? WorkflowMessage.TYPE_REJECT
							: WorkflowMessage.TYPE_COMMENT;
						message.message = event.comment;
						message.user = event.user;
						message.date = event.date;
						message.node = itemStatus;
						session.save(message);
					}
				}
			}
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from WorkflowNode where type = 't'")
			+ count(session, "from Item where moderating = true or status = 'REJECTED'");
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getAddIndexesAndConstraintsForColumns(STATUS_TABLE, "date_due", "cause_id"));
		sql.addAll(helper.getDropColumnSQL("moderation_status", "overall_escalation"));
		return sql;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getCreationSql(new TablesOnlyFilter("workflow_message")));
		sql.addAll(helper.getAddColumnsSQL(TABLE, "priority", "auto_action", "action_days", "due_date_path",
			"due_date_schema_uuid", "movelive_accept"));
		sql.addAll(helper.getAddColumnsSQL(STATUS_TABLE, "overdue", "cause_id"));
		sql.addAll(helper.getAddColumnsSQL(MODERATION_TABLE, "rejected_message", "rejected_by", "rejected_step"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeWorkflowItem.class, FakeWorkflowMessage.class, FakeWorkflowItemStatus.class,
				FakeModerationStatus.class, FakeItem.class, FakeCollection.class, FakeHistoryEvent.class};
	}

	@Entity(name = "WorkflowNode")
	public static class FakeWorkflowItem
	{
		@Id
		long id;

		String uuid;
		Character type;
		Boolean movelive;
		Boolean moveliveAccept;
		Integer priority;
		Boolean autoAction;
		Integer actionDays;
		Boolean escalate;
		Integer escalationdays;

		@Column(length = 512)
		String dueDatePath;
		@Column(length = 40)
		String dueDateSchemaUuid;
		long workflowId;
	}

	@Entity(name = "WorkflowMessage")
	@AccessType("field")
	public static class FakeWorkflowMessage
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		char type;
		@Column(nullable = false)
		Date date;

		@ManyToOne
		@Index(name = "messagenode_idx")
		FakeWorkflowItemStatus node;
		@Lob
		@Column(nullable = false)
		String message;
		@Column(length = 255, nullable = false)
		String user;
	}

	@Entity(name = "WorkflowNodeStatus")
	@AccessType("field")
	public static class FakeWorkflowItemStatus
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		String acttype;
		char status;
		@Index(name = "datedue_idx")
		Date dateDue;
		Date started;
		Boolean overdue;
		@ManyToOne
		@Index(name = "cause_idx")
		FakeWorkflowItemStatus cause;
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "mod_status_id", nullable = false)
		FakeModerationStatus modStatus;
		@ManyToOne
		FakeWorkflowItem wnode;
	}

	@Entity(name = "ModerationStatus")
	@AccessType("field")
	public static class FakeModerationStatus
	{
		@Id
		long id;
		@Lob
		String rejectedMessage;
		@Column(length = 255)
		String rejectedBy;
		@Column(length = 40)
		String rejectedStep;
		Date overallEscalation;
		@OneToMany(fetch = FetchType.LAZY, mappedBy = "modStatus")
		Set<FakeWorkflowItemStatus> statuses;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		long id;
		boolean moderating;
		String status;

		@OneToMany
		@IndexColumn(name = "histindex")
		List<FakeHistoryEvent> history;
		@OneToOne
		FakeModerationStatus moderation;
		@ManyToOne
		FakeCollection itemDefinition;
		Date dateForIndex;
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeCollection
	{
		@Id
		long id;
		Long workflowId;
	}

	@Entity(name = "HistoryEvent")
	@AccessType("field")
	public static class FakeHistoryEvent
	{
		@Id
		long id;
		Date date;
		String type;

		String user;
		String step;
		@Lob
		String comment;
	}
}
