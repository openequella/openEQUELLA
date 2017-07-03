package com.tle.core.workflow.migrate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.old.workflow.node.WorkflowTreeNode;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.workflow.migrate.beans.FakeWorkflow;
import com.tle.core.workflow.migrate.beans.FakeWorkflowItemStatus;
import com.tle.core.workflow.migrate.beans.FakeWorkflowNodeStatus;
import com.tle.core.workflow.migrate.beans.node.FakeDecisionNode;
import com.tle.core.workflow.migrate.beans.node.FakeParallelNode;
import com.tle.core.workflow.migrate.beans.node.FakeSerialNode;
import com.tle.core.workflow.migrate.beans.node.FakeWorkflowItem;
import com.tle.core.workflow.migrate.beans.node.FakeWorkflowNode;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
public class CreateWorkflowTables extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(CreateWorkflowTables.class) + "."; //$NON-NLS-1$

	private static final Log LOGGER = LogFactory.getLog(CreateWorkflowTables.class);

	@Inject
	private XmlService xmlService;

	@Override
	@SuppressWarnings("nls")
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Workflow") + count(session, "FROM WorkflowNodeStatus");
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		ClassLoader classLoader = getClass().getClassLoader();
		XStream xstream = xmlService.createDefault(classLoader);
		xstream.aliasPackage("com.tle.beans.entity.workflow", "com.tle.common.old.workflow");

		MigrateWorkflow migrator = new MigrateWorkflow();
		Map<Long, Map<String, FakeWorkflowNode>> workflowMap = new HashMap<Long, Map<String, FakeWorkflowNode>>();
		Query allWorkflows = session.createQuery("from Workflow");
		List<FakeWorkflow> workflows = allWorkflows.list();
		for( FakeWorkflow workflow : workflows )
		{

			List<FakeWorkflowNode> newNodes = migrator.convertNodes((WorkflowTreeNode) xstream.fromXML(workflow.root));
			for( FakeWorkflowNode newNode : newNodes )
			{
				newNode.setWorkflow(workflow);
				session.save(newNode);
				session.flush();
			}
			result.incrementStatus();
			workflowMap.put(workflow.id, workflow.getAllTasksAsMap());
		}
		session.clear();

		int badModStatus = 0;
		Query allStatuses = session.createQuery("from WorkflowNodeStatus");
		List<FakeWorkflowNodeStatus> statuses = allStatuses.list();
		for( FakeWorkflowNodeStatus status : statuses )
		{
			String nodeId = status.nodeId;
			Query statusQuery = session.createQuery("select s from ModerationStatus s join s.statuses st where st = ?");
			statusQuery.setParameter(0, status);
			FakeModerationStatus modStatus = (FakeModerationStatus) statusQuery.uniqueResult();
			if( modStatus != null )
			{
				FakeItem item = modStatus.item;
				if( item != null )
				{
					Map<String, FakeWorkflowNode> map = workflowMap.get(item.itemDefinition.workflowId);
					FakeWorkflowNode linkedNode = map != null ? map.get(nodeId) : null;
					if( linkedNode != null )
					{
						status.setNode(linkedNode);
						status.setModStatus(modStatus);
						if( status instanceof FakeWorkflowItemStatus )
						{
							FakeWorkflowItemStatus itemStatus = (FakeWorkflowItemStatus) status;
							itemStatus.setAcceptedUsers((Set<String>) xstream.fromXML(itemStatus.oldAccepted));
						}
						session.save(status);
					}
					else
					{
						modStatus.statuses.remove(status);
						session.save(modStatus);
					}
				}
				else
				{
					badModStatus++;
					session.delete(modStatus);
				}
			}
			result.incrementStatus();
			session.flush();
		}
		session.clear();
		int numDeleted = session.createQuery("delete from WorkflowNodeStatus where node is null").executeUpdate();
		if( numDeleted > 0 )
		{
			LOGGER.warn("Found " + numDeleted + " orphaned WorkflowNodeStatus objects");
		}
		if( badModStatus > 0 )
		{
			LOGGER.warn("Found " + badModStatus + " orphaned ModerationStatus objects");
		}
		session.createQuery("update ModerationStatus set needsReset = false").executeUpdate();
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> creationSql = helper
			.getCreationSql(new TablesOnlyFilter("workflow_node", "workflow_node_auto_assigns", "workflow_node_groups",
				"workflow_node_roles", "workflow_node_users", "workflow_node_status_accepted"));
		creationSql.addAll(helper.getAddColumnsSQL("workflow_node_status", "mod_status_id"));
		creationSql.addAll(helper.getAddColumnsSQL("workflow_node_status", "wnode_id"));
		creationSql.addAll(helper.getAddColumnsSQL("moderation_status", "needs_reset"));
		return creationSql;
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sqlDrops = helper.getDropColumnSQL("workflow", "root");
		sqlDrops.addAll(helper.getDropTableSql("workflow_all_groups"));
		sqlDrops.addAll(helper.getDropTableSql("moderation_status_statuses"));
		sqlDrops.addAll(helper.getDropColumnSQL("workflow_node_status", "type", "node_id", "accepted_users"));
		sqlDrops.addAll(helper.getAddNotNullSQL("workflow_node_status", "wnode_id"));
		sqlDrops.addAll(helper.getAddNotNullSQL("workflow_node_status", "mod_status_id"));
		sqlDrops.addAll(helper.getAddNotNullSQL("moderation_status", "needs_reset"));
		sqlDrops.addAll(helper.getAddIndexesAndConstraintsForColumns("workflow_node_status", "wnode_id"));
		sqlDrops.addAll(helper.getAddIndexesAndConstraintsForColumns("workflow_node_status", "mod_status_id"));
		return sqlDrops;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeWorkflow.class, FakeWorkflowNodeStatus.class, FakeWorkflowItemStatus.class,
				Institution.class, LanguageBundle.class, LanguageString.class, FakeWorkflowItem.class,
				FakeDecisionNode.class, FakeSerialNode.class, FakeParallelNode.class, OldWorkflowGroups.class,
				FakeItem.class, FakeModerationStatus.class, FakeItemDefinition.class, FakeWorkflowNode.class};
	}

	@Override
	@SuppressWarnings("nls")
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migrate.title", keyPrefix + "migrate.description");
	}

	@AccessType("field")
	@Entity(name = "WorkflowAllGroups")
	public static class OldWorkflowGroups
	{
		@Id
		long id;
	}

	@AccessType("field")
	@Entity(name = "ModerationStatusStatuses")
	public static class OldModStatuses
	{
		@Id
		long id;
	}

	@AccessType("field")
	@Entity(name = "Item")
	public static class FakeItem
	{
		@Id
		long id;
		@OneToOne
		FakeModerationStatus moderation;
		@ManyToOne
		FakeItemDefinition itemDefinition;
	}

	@AccessType("field")
	@Entity(name = "ItemDefinition")
	public static class FakeItemDefinition
	{
		@Id
		long id;
		Long workflowId;
	}
}
