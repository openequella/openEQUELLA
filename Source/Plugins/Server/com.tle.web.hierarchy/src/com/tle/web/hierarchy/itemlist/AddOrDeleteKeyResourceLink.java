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

package com.tle.web.hierarchy.itemlist;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.hierarchy.VirtualTopicUtils;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.security.TLEAclManager;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.hierarchy.section.TopicDisplaySection;
import com.tle.web.hierarchy.section.TopicDisplaySection.ExtendedTopicDisplayModel;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
@SuppressWarnings("nls")
public class AddOrDeleteKeyResourceLink extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("itemlist.addtotopiclink")
	private static Label ADD_TO_TOPIC_LABEL;

	@PlugKey("itemlist.removefromtopiclink")
	private static Label REMOVE_KEY_RESOURCE_LABEL;

	@TreeLookup
	private TopicDisplaySection topicDisplay;
	@EventFactory
	private EventGenerator events;
	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		ListSettings<ItemListEntry> listSettings)
	{
		if( CurrentUser.wasAutoLoggedIn() )
		{
			return null;
		}

		ExtendedTopicDisplayModel model = topicDisplay.getModel(context);
		final HierarchyTopic currentTopic = model.getTopic();
		final String topicValue = model.getTopicValue();
		final Map<String, String> values = model.getValues();

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				final Set<String> privilege = aclManager.filterNonGrantedPrivileges(currentTopic,
					Collections.singleton("MODIFY_KEY_RESOURCE"));

				HtmlLinkState link = null;
				if( !privilege.isEmpty() )
				{
					ItemId itemId = entry.getItem().getItemId();
					String topicId = VirtualTopicUtils.buildTopicId(currentTopic, topicValue, values);

					if( entry.isHilighted() )
					{
						link = new HtmlLinkState(REMOVE_KEY_RESOURCE_LABEL, events.getNamedHandler("removeKeyResource",
							itemId, topicId));
					}
					else
					{
						link = new HtmlLinkState(ADD_TO_TOPIC_LABEL, events.getNamedHandler("addAsKeyResource", itemId,
							topicId));
					}
					entry.addRatingMetadata(link);
				}

			}
		};
	}

	@EventHandlerMethod
	public void removeKeyResource(SectionInfo info, ItemId itemId, String topicId)
	{
		hierarchyService.deleteKeyResources(topicId, itemId);
	}

	@EventHandlerMethod
	public void addAsKeyResource(SectionInfo info, ItemId itemId, String topicId)
	{
		hierarchyService.addKeyResource(topicId, itemId);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
