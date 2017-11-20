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
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.hierarchy.addkey.HierarchyTreeSection;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.searching.itemlist.GalleryItemList;
import com.tle.web.searching.itemlist.VideoItemList;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind
@SuppressWarnings("nls")
public class AddToHierarchyLink extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("itemlist.addtolink")
	private static Label ADD_TO_HIERARCHY_LABEL;

	@EventFactory
	private EventGenerator events;

	@Inject
	private TLEAclManager aclManager;

	@Inject
	private ViewItemUrlFactory urlFactory;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		final ListSettings<ItemListEntry> listSettings)
	{
		if( CurrentUser.wasAutoLoggedIn() )
		{
			return null;
		}
		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				if( entry.isFlagSet("com.tle.web.hierarchy.DontShow") )
				{
					return;
				}

				final Set<String> privilege = aclManager.filterNonGrantedPrivileges(Collections
					.singleton("MODIFY_KEY_RESOURCE"));
				if( !privilege.isEmpty() )
				{
					attachHierarchyLink(entry, listSettings.getAttribute(GalleryItemList.GALLERY_FLAG) != null
						|| listSettings.getAttribute(VideoItemList.VIDEO_FLAG) != null);
				}

			}
		};
	}

	private HtmlLinkState attachHierarchyLink(ItemListEntry entry, boolean gallery)
	{
		HtmlLinkState link;
		final Item item = entry.getItem();
		if( gallery )
		{
			link = new HtmlLinkState(new IconLabel(Icon.HIERACHY, null, true), events.getNamedHandler(
				"goToHierarchyPage", item.getItemId()));
			link.setTitle(ADD_TO_HIERARCHY_LABEL);
			link.addClass("gallery-action");
			entry.addRatingMetadataWithOrder(200, new LinkRenderer(link));
		}
		else
		{
			link = new HtmlLinkState(ADD_TO_HIERARCHY_LABEL, events.getNamedHandler("goToHierarchyPage",
				item.getItemId()));
			entry.addRatingMetadata(link);
		}
		return link;
	}

	@EventHandlerMethod
	public void goToHierarchyPage(SectionInfo info, String itemId)
	{
		ViewItemUrl hierarchyPageUrl = urlFactory.createItemUrl(info, new ItemId(itemId));
		SectionInfo viewInfo = hierarchyPageUrl.getSectionInfo();
		ItemSummaryContentSection summary = viewInfo.lookupSection(ItemSummaryContentSection.class);
		summary.setSummaryId(viewInfo, viewInfo.lookupSection(HierarchyTreeSection.class));
		hierarchyPageUrl.forward(info);
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
