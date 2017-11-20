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

package com.tle.core.migration.initial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.classic.Session;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.tle.beans.ConfigurationProperty;
import com.tle.beans.Institution;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.Language;
import com.tle.beans.ReferencedURL;
import com.tle.beans.SchemaScript;
import com.tle.beans.Staging;
import com.tle.beans.UserPreference;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemdefBlobs;
import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Comment;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemLock;
import com.tle.beans.item.ItemXml;
import com.tle.beans.item.ModerationStatus;
import com.tle.beans.item.Relation;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.NavigationSettings;
import com.tle.beans.item.attachments.ZipAttachment;
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
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.SerialNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.AllDataHibernateMigrationFilter;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.beans.SystemConfig;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

@Bind
@Singleton
public class InitialSchema extends AbstractCreateMigration
{
	private PluginTracker<Object> initialTracker;

	private static Class<?>[] clazzes = new Class<?>[]{ConfigurationProperty.class, ItemDefinitionScript.class,
			SchemaScript.class, Institution.class, Language.class, Staging.class, UserPreference.class,
			UserPreference.UserPrefKey.class, AuditLogEntry.class, BaseEntity.class, LanguageBundle.class,
			LanguageString.class, EntityLock.class, ItemDefinition.class, ItemdefBlobs.class, Workflow.class,
			WorkflowNodeStatus.class, WorkflowItemStatus.class, DecisionNode.class, ScriptNode.class,
			ParallelNode.class, SerialNode.class, WorkflowItem.class, WorkflowNode.class, ReferencedURL.class,
			Comment.class, DrmAcceptance.class, HistoryEvent.class, Item.class, ItemXml.class, DrmSettings.class,
			ItemLock.class, ModerationStatus.class, CourseInfo.class, Attachment.class, IMSResourceAttachment.class,
			FileAttachment.class, HtmlAttachment.class, ImsAttachment.class, CustomAttachment.class,
			LinkAttachment.class, ZipAttachment.class, ItemNavigationNode.class, ItemNavigationTab.class,
			NavigationSettings.class, AccessEntry.class, AccessExpression.class, SharePass.class, TLEUser.class,
			TLEGroup.class, Relation.class, Bookmark.class, MimeEntry.class, ActivateRequest.class,
			TargetListEntry.class, VersionSelection.class, BaseEntity.Attribute.class, ACLEntryMapping.class};

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.migration.initial.title", "com.tle.core.migration.initial.description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		List<Class<?>> classList = new ArrayList<Class<?>>(Arrays.asList(clazzes));
		List<Extension> extensions = initialTracker.getExtensions();
		for( Extension extension : extensions )
		{
			Collection<Parameter> tempClazzes = extension.getParameters("class"); //$NON-NLS-1$
			for( Parameter parameter : tempClazzes )
			{
				String clazzName = parameter.valueAsString();
				classList.add(initialTracker.getClassForName(extension, clazzName));
			}
		}
		return classList.toArray(new Class<?>[classList.size()]);
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		initialTracker = new PluginTracker<Object>(pluginService, "com.tle.core.migration", "initialSchema", null); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	protected void addExtraStatements(HibernateMigrationHelper helper, List<String> sql)
	{
		sql.addAll(helper.getAddIndexesRaw("bookmark_keywords", "bookkeywords", "bookmark_id"));

		sql.addAll(helper.getAddIndexesRaw("tlegroup_users", new String[]{"tleguGroup", "tlegroup_id"}, new String[]{
				"tleguElem", "element"}));
		sql.addAll(helper.getAddIndexesRaw("tlegroup_all_parents", "tlegap_parent", "all_parents_id"));

		sql.addAll(helper.getAddIndexesRaw("item_collaborators", "ic_item", "item_id"));
		sql.addAll(helper.getAddIndexesRaw("item_notifications", "in_item", "item_id"));
		sql.addAll(helper.getAddIndexesRaw("base_entity_attributes", "bea_entity", "base_entity_id"));

		sql.addAll(helper.getAddIndexesRaw("access_expression_expression_p", "aeep_aexp", "access_expression_id"));

		sql.addAll(helper.getAddIndexesRaw("workflow_node_users", "wnu_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRaw("workflow_node_groups", "wng_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRaw("workflow_node_roles", "wnr_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRaw("workflow_node_auto_assigns", "wnaa_node", "workflow_node_id"));
		sql.addAll(helper.getAddIndexesRaw("workflow_node_status_accepted", "taskAcceptedNode",
			"workflow_node_status_id"));

		sql.addAll(helper.getAddIndexesRaw("mime_entry_extensions", "mee_mime", "mime_entry_id"));

		List<Extension> extensions = initialTracker.getExtensions();
		for( Extension extension : extensions )
		{
			Collection<Parameter> indexes = extension.getParameters("index");
			for( Parameter indexParam : indexes )
			{
				String table = indexParam.getSubParameter("table").valueAsString();
				String name = indexParam.getSubParameter("name").valueAsString();
				Collection<Parameter> cols = indexParam.getSubParameters("column");
				String function = indexParam.getSubParameter("function") != null ? indexParam.getSubParameter(
					"function").valueAsString() : null;
				String[] index = new String[cols.size() + 1];
				index[0] = name;
				int i = 1;
				for( Parameter col : cols )
				{
					index[i++] = col.valueAsString();
				}
				if( function == null )
				{
					sql.addAll(helper.getAddIndexesRaw(table, index));
				}
				else
				{
					sql.addAll(helper.getAddFunctionIndexes(table, function, index));
				}
			}
		}
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		AllDataHibernateMigrationFilter filter = new AllDataHibernateMigrationFilter();
		Session session = helper.getFactory().openSession();
		if( helper.tableExists(session, SystemConfig.TABLE_NAME) )
		{
			filter.setIncludeGenerators(false);
		}
		session.close();
		return filter;
	}
}
