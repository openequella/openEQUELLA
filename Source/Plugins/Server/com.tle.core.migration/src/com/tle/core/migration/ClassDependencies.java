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

package com.tle.core.migration;

import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.beans.ReferencedURL;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.SchemaTransform;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemdefBlobs;
import com.tle.beans.entity.schema.Citation;
import com.tle.beans.item.Comment;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemXml;
import com.tle.beans.item.ModerationStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.NavigationSettings;
import com.tle.beans.security.SharePass;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author Aaron
 */
public class ClassDependencies
{
	public static Set<Class<?>> item()
	{
		final Set<Class<?>> deps = Sets.newHashSet();
		deps.add(Item.class);
		deps.add(Attachment.class);
		deps.add(LanguageBundle.class);
		deps.add(LanguageString.class);
		deps.add(ReferencedURL.class);
		deps.add(DrmSettings.class);
		deps.add(ModerationStatus.class);
		deps.add(WorkflowNodeStatus.class);
		deps.add(WorkflowMessage.class);
		deps.add(HistoryEvent.class);
		deps.add(Comment.class);
		deps.add(SharePass.class);
		deps.add(DrmAcceptance.class);
		deps.add(ItemNavigationNode.class);
		deps.add(ItemNavigationTab.class);
		deps.add(NavigationSettings.class);
		deps.add(ItemXml.class);
		deps.addAll(collection());
		deps.addAll(schema());
		return deps;
	}

	public static Set<Class<?>> collection()
	{
		final Set<Class<?>> deps = Sets.newHashSet();
		deps.add(ItemDefinition.class);
		deps.add(ItemdefBlobs.class);
		deps.addAll(workflow());
		deps.addAll(baseEntity());
		return deps;
	}

	public static Set<Class<?>> schema()
	{
		final Set<Class<?>> deps = Sets.newHashSet();
		deps.add(Schema.class);
		deps.add(SchemaTransform.class);
		deps.add(Citation.class);
		deps.addAll(baseEntity());
		return deps;
	}

	public static Set<Class<?>> workflow()
	{
		final Set<Class<?>> deps = Sets.newHashSet();
		deps.add(Workflow.class);
		deps.add(WorkflowNode.class);
		deps.addAll(baseEntity());
		return deps;
	}

	public static Set<Class<?>> baseEntity()
	{
		final Set<Class<?>> deps = Sets.newHashSet();
		deps.add(BaseEntity.class);
		deps.add(BaseEntity.Attribute.class);
		deps.add(LanguageBundle.class);
		deps.add(LanguageString.class);
		return deps;
	}

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	public ClassDependencies()
	{
		throw new Error();
	}
}
