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

package com.tle.core.legacy.initial;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.Language;
import com.tle.beans.ReferencedURL;
import com.tle.beans.SchemaScript;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.SchemaTransform;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemdefBlobs;
import com.tle.beans.entity.schema.Citation;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Comment;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemXml;
import com.tle.beans.item.ModerationStatus;
import com.tle.beans.item.Relation;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.NavigationSettings;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.beans.security.SharePass;
import com.tle.beans.user.TLEGroup;
import com.tle.common.security.TargetListEntry;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class EnsureForeignKeyIndexes extends AbstractHibernateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(EnsureForeignKeyIndexes.class) + ".ensurefki."; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		status.setupSubTaskStatus(AbstractCreateMigration.KEY_CALCULATING, 100);
		status.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> sql = new ArrayList<String>();
		Session session = helper.getFactory().openSession();

		sql.addAll(helper.getAddIndexesIfRequired(session, "hierarchy_topic", "parentTopic", "hiearchyPowerSearch",
			"hierarchyName", "hierarchyShortDescription", "hierarchyLongDescription", "hierarchySName",
			"hierarchyRSName"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "tlegroup", "parentGroup"));
		sql.addAll(helper.getAddIndexesIfRequired(session, "workflow_node_status", "nodeStatusModStatusIndex",
			"nodeStatusNodeIndex"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "workflow_node", "workflowNodeName", "workflowNodeWorkflow",
			"workflowNodeParent", "workflowNodeDesc"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "item", "itemModerationStatus", "itemName",
			"itemDescription", "itemItemDefinition", "itemDrmSettings"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "audit_log_entry", "auditInstituion"));
		sql.addAll(helper.getAddIndexesIfRequired(session, "activate_request", "activateRequestItem",
			"activateRequestCourse"));
		sql.addAll(helper.getAddIndexesIfRequired(session, "bookmark", "bookmarkItem"));
		sql.addAll(helper.getAddIndexesIfRequired(session, "base_entity", "baseEntityName", "baseEntityDescription"));
		sql.addAll(helper.getAddIndexesIfRequired(session, "access_entry", "accessEntryExpression",
			"accessEntryInstitution"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "share_pass", "sharePassItem", "sharePassInstitution"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "item_definition", "collectionSchema", "collectionWorkflow",
			"collectionBlobs"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "language", "languageInstitution"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "item_navigation_node", "itemNavNodeItem",
			"itemNavNodeParent"));
		sql.addAll(helper.getAddIndexesIfRequired(session, "power_search", "powerSearchSchema"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "item_navigation_tab", "itemNavTabNode",
			"itemNavTabAttachment"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "drm_acceptance", "drmAcceptItem"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "comments", "commentItem"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "relation", "relationFirstItem", "relationSecondItem"));
		session.close();
		runSqlStatements(sql, helper.getFactory(), status, AbstractCreateMigration.KEY_STATUS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, Item.class, LanguageBundle.class, DrmSettings.class,
				ItemDefinition.class, Schema.class, ItemXml.class, ModerationStatus.class, ItemdefBlobs.class,
				Workflow.class, WorkflowNode.class, WorkflowNodeStatus.class, ItemNavigationNode.class,
				HistoryEvent.class, ReferencedURL.class, Attachment.class, DrmAcceptance.class, Comment.class,
				SharePass.class, LanguageString.class, ItemNavigationTab.class, AuditLogEntry.class, Bookmark.class,
				CourseInfo.class, HierarchyTopic.class, PowerSearch.class, TLEGroup.class, WorkflowItem.class,
				ActivateRequest.class, AccessEntry.class, AccessExpression.class, Language.class, Relation.class,
				WorkflowMessage.class, BaseEntity.class, BaseEntity.Attribute.class, VersionSelection.class,
				NavigationSettings.class, SchemaTransform.class, ItemDefinitionScript.class,
				HierarchyTopic.Attribute.class, Citation.class, SchemaScript.class, TargetListEntry.class,
				ACLEntryMapping.class};
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

}
