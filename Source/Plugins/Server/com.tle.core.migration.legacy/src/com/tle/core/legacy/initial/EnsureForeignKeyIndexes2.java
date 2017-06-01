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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.Language;
import com.tle.beans.ReferencedURL;
import com.tle.beans.SchemaScript;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.DynaCollection;
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
import com.tle.beans.mime.MimeEntry;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.beans.security.SharePass;
import com.tle.beans.user.TLEGroup;
import com.tle.common.security.TargetListEntry;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
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
public class EnsureForeignKeyIndexes2 extends AbstractHibernateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(EnsureForeignKeyIndexes2.class)
		+ ".ensurefki2."; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		status.setupSubTaskStatus(AbstractCreateMigration.KEY_CALCULATING, 100);
		status.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> sql = new ArrayList<String>();
		Session session = helper.getFactory().openSession();

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "bookmark_keywords", "bookkeywords", "bookmark_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "power_search_itemdefs", new String[]{"psid_search",
				"power_search_id"}, new String[]{"psid_itemdef", "itemdefs_id"}));

		sql.addAll(helper.getAddIndexesIfRequired(session, "audit_log_entry", "auditTime", "auditCat", "auditType",
			"auditUser", "auditSession", "auditData1", "auditData2", "auditData3"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "hierarchy_topic_inh_schemas", new String[]{
				"htis_schema", "entity_id"}, new String[]{"htis_topic", "hierarchy_topic_id"}));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "hierarchy_topic_all_parents", "htap_parent",
			"all_parents_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "hierarchy_topic_inh_idefs", new String[]{"htii_entity",
				"entity_id"}, new String[]{"htii_topic", "hierarchy_topic_id"}));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "hierarchy_topic_add_idefs", new String[]{"htai_topic",
				"hierarchy_topic_id"}, new String[]{"htai_entity", "entity_id"}));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "hierarchy_topic_key_resources", new String[]{
				"htkr_item", "element"}, new String[]{"htkr_topic", "hierarchy_topic_id"}));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "hierarchy_topic_add_schemas", new String[]{
				"htas_schema", "entity_id"}, new String[]{"htas_topic", "hierarchy_topic_id"}));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "tlegroup_users", "tleguGroup", "tlegroup_id"));
		sql.addAll(helper
			.getAddIndexesRawIfRequired(session, "tlegroup_all_parents", "tlegap_parent", "all_parents_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "item_collaborators", "ic_item", "item_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "item_notifications", "in_item", "item_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "base_entity_attributes", "bea_entity", "base_entity_id"));

		sql.addAll(helper.getAddIndexesIfRequired(session, "attachment", "attachmentItem"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "dyna_collection_item_defs", new String[]{"dcid_entity",
				"entity_id"}, new String[]{"dcid_dyna", "dyna_collection_id"}));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "dyna_collection_schemas", new String[]{"dcs_dyna",
				"dyna_collection_id"}, new String[]{"dcs_entity", "entity_id"}));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "dyna_collection_usage_ids", "dcui_dyna",
			"dyna_collection_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "schema_exp_transforms", "set_schema", "schema_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "schema_imp_transforms", "sit_schema", "schema_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "schema_citations", "sc_schema", "schema_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "access_expression_expression_p", "aeep_aexp",
			"access_expression_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "workflow_node_users", "wnu_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "workflow_node_groups", "wng_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "workflow_node_roles", "wnr_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "workflow_node_auto_assigns", "wnaa_node",
			"workflow_node_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "workflow_node_status_accepted", "taskAcceptedNode",
			"workflow_node_status_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "mime_entry_extensions", "mee_mime", "mime_entry_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "portlet_recent_contrib_collect", new String[]{
				"portrc_contrib", "portlet_recent_contrib_id"}, new String[]{"portrc_collect", "collections_id"}));
		sql.addAll(helper.getAddIndexesIfRequired(session, "portlet_recent_contrib", "portrc_portlet"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cla_holding_ids", "clahi_holding", "cla_holding_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cla_holding_authors", "claha_holding", "cla_holding_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cla_portion_topics", "clapt_portion", "cla_portion_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cla_portion_authors", "clapa_portion", "cla_portion_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cal_holding_ids", "calhi_holding", "cal_holding_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cal_holding_authors", "calha_holding", "cal_holding_id"));

		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cal_portion_topics", "calpt_portion", "cal_portion_id"));
		sql.addAll(helper.getAddIndexesRawIfRequired(session, "cal_portion_authors", "calpa_portion", "cal_portion_id"));

		session.close();
		runSqlStatements(sql, helper.getFactory(), status, AbstractCreateMigration.KEY_STATUS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, Item.class, LanguageBundle.class, DrmSettings.class,
				ItemDefinition.class, Schema.class, ItemXml.class, ModerationStatus.class, ItemdefBlobs.class,
				ItemDefinitionScript.class, TargetListEntry.class, SchemaScript.class, Workflow.class,
				WorkflowNode.class, WorkflowNodeStatus.class, WorkflowItemStatus.class, ItemNavigationNode.class,
				HistoryEvent.class, ReferencedURL.class, Attachment.class, DrmAcceptance.class, Comment.class,
				SharePass.class, LanguageString.class, ItemNavigationTab.class, AuditLogEntry.class, Bookmark.class,
				CourseInfo.class, HierarchyTopic.class, PowerSearch.class, TLEGroup.class, WorkflowItem.class,
				ActivateRequest.class, AccessEntry.class, AccessExpression.class, Language.class, Relation.class,
				Bookmark.class, FakePortletRecentContrib.class, FakePortlet.class, FakeCLAHolding.class,
				DynaCollection.class, FakeCLAPortion.class, FakeCALPortion.class, FakeCALHolding.class,
				MimeEntry.class, WorkflowMessage.class, NavigationSettings.class, HierarchyTopic.Attribute.class,
				ACLEntryMapping.class, BaseEntity.class, VersionSelection.class, BaseEntity.Attribute.class,
				Citation.class, SchemaTransform.class};
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	@Entity(name = "PortletRecentContrib")
	@AccessType("field")
	public static class FakePortletRecentContrib implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Id
		long id;

		@OneToOne
		@JoinColumn(nullable = false)
		@Index(name = "portrc_portlet")
		FakePortlet portlet;

		@ManyToMany(fetch = FetchType.EAGER)
		Collection<ItemDefinition> collections;
	}

	@Entity(name = "Portlet")
	public static class FakePortlet
	{
		@Id
		long id;
	}

	@Entity(name = "CLA_Holding")
	@AccessType("field")
	public static class FakeCLAHolding
	{
		@Id
		long id;

		@ElementCollection
		@Column(name = "element")
		@CollectionTable(name = "cla_holding_ids", joinColumns = @JoinColumn(name = "cla_holding_id"))
		List<String> ids;
		@ElementCollection
		@Column(name = "element")
		@CollectionTable(name = "cla_holding_authors", joinColumns = @JoinColumn(name = "cla_holding_id"))
		List<String> authors;
	}

	@Entity(name = "CLA_Portion")
	@AccessType("field")
	public static class FakeCLAPortion
	{
		@Id
		long id;

		@ElementCollection
		@Column(name = "element")
		@CollectionTable(name = "cla_portion_authors", joinColumns = @JoinColumn(name = "cla_portion_id"))
		List<String> authors;
		@ElementCollection
		@Column(name = "element")
		@CollectionTable(name = "cla_portion_topics", joinColumns = @JoinColumn(name = "cla_portion_id"))
		List<String> topics;
	}

	@Entity(name = "CAL_Holding")
	@AccessType("field")
	public static class FakeCALHolding
	{
		@Id
		long id;

		@ElementCollection
		@CollectionTable(name = "cal_holding_ids", joinColumns = @JoinColumn(name = "cal_holding_id"))
		@Column(name = "element")
		List<String> ids;
		@ElementCollection
		@CollectionTable(name = "cal_holding_authors", joinColumns = @JoinColumn(name = "cal_holding_id"))
		@Column(name = "element")
		List<String> authors;
	}

	@Entity(name = "CAL_Portion")
	@AccessType("field")
	public static class FakeCALPortion
	{
		@Id
		long id;

		@ElementCollection
		@Column(name = "element")
		@CollectionTable(name = "cal_portion_authors", joinColumns = @JoinColumn(name = "cal_portion_id"))
		List<String> authors;
		@ElementCollection
		@Column(name = "element")
		@CollectionTable(name = "cal_portion_topics", joinColumns = @JoinColumn(name = "cal_portion_id"))
		List<String> topics;
	}
}
