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
import com.tle.beans.mime.MimeEntry;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.beans.security.SharePass;
import com.tle.beans.user.TLEGroup;
import com.tle.beans.user.TLEUser;
import com.tle.common.security.TargetListEntry;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class AddNullConstraints extends AbstractHibernateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddNullConstraints.class) + ".addnulls."; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		status.setupSubTaskStatus(AbstractCreateMigration.KEY_CALCULATING, 100);
		status.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> sql = new ArrayList<String>();
		Session session = helper.getFactory().openSession();
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "access_entry", "expression_id", "institution_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "activate_request", "attachment", "type", "uuid",
			"course_info_id", "item_id"));

		ExtendedDialect extDialect = helper.getExtDialect();
		if( extDialect.requiresNoConstraintsForModify() )
		{
			sql.addAll(helper.getDropConstraintsSQL("attachment", "uuid"));
			sql.addAll(helper.getDropConstraintsSQL("base_entity", "uuid", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("bookmark", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("hierarchy_topic", "uuid", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("institution", "short_name", "url"));
			sql.addAll(helper.getDropConstraintsSQL("item", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("language_string", "bundle_id"));
			sql.addAll(helper.getDropConstraintsSQL("mime_entry", "type", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("tlegroup", "uuid", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("tleuser", "uuid", "institution_id"));
			sql.addAll(helper.getDropConstraintsSQL("comments", "item_id"));
		}

		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "attachment", "uuid", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "audit_log_entry", "user_id"));

		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "base_entity", "date_created", "date_modified", "uuid",
			"institution_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "bookmark", "owner", "institution_id", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "comments", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "drm_acceptance", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "drm_settings", "drm_page_uuid"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "hierarchy_topic", "uuid", "institution_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "institution", "admin_password", "badge_url",
			"short_name", "name"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "item", "owner", "institution_id", "item_definition_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "item_navigation_node", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "item_navigation_tab", "node_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "language", "country", "language", "variant",
			"institution_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "language_string", "bundle_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "mime_entry", "type", "institution_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "moderation_status", "needs_reset"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "relation", "first_item_id", "second_item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "share_pass", "creator", "email_address", "privilege",
			"institution_id", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "tlegroup", "uuid", "institution_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "tleuser", "uuid", "institution_id", "username",
			"password", "first_name", "last_name"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "workflow_node", "uuid"));

		if( extDialect.requiresNoConstraintsForModify() )
		{
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("attachment", "uuid"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("base_entity", "uuid", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("bookmark", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("hierarchy_topic", "uuid", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("institution", "short_name", "url"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("item", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("language_string", "bundle_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("mime_entry", "type", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("tlegroup", "uuid", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("tleuser", "uuid", "institution_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("comments", "item_id"));
		}

		session.close();
		runSqlStatements(sql, helper.getFactory(), status, AbstractCreateMigration.KEY_STATUS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{AccessEntry.class, ActivateRequest.class, AccessExpression.class, Institution.class,
				CourseInfo.class, Item.class, LanguageBundle.class, DrmSettings.class, ItemDefinition.class,
				Schema.class, ItemXml.class, ModerationStatus.class, ItemdefBlobs.class, Workflow.class,
				WorkflowNode.class, WorkflowNodeStatus.class, ItemNavigationNode.class, HistoryEvent.class,
				ReferencedURL.class, Attachment.class, DrmAcceptance.class, Comment.class, SharePass.class,
				LanguageString.class, ItemNavigationTab.class, AuditLogEntry.class, Bookmark.class,
				HierarchyTopic.class, PowerSearch.class, Language.class, MimeEntry.class, Relation.class,
				SharePass.class, TLEGroup.class, TLEUser.class, WorkflowMessage.class, NavigationSettings.class,
				SchemaTransform.class, ItemDefinitionScript.class, HierarchyTopic.Attribute.class, Citation.class,
				SchemaScript.class, ACLEntryMapping.class, TargetListEntry.class, VersionSelection.class,
				BaseEntity.class, BaseEntity.Attribute.class};
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

}
