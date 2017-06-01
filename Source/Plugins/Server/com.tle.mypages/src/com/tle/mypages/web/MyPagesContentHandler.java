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

package com.tle.mypages.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.guice.Bind;
import com.tle.encoding.UrlEncodedString;
import com.tle.mycontent.SimpleContentHandler;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class MyPagesContentHandler extends SimpleContentHandler<MyPagesContributeSection>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(MyPagesContentHandler.class);

	private static final Label TITLE = new KeyLabel(RESOURCES.key("handler"));

	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	@Named("myPagesContentTree")
	private Provider<SectionTree> handlerTree;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Override
	protected SectionTree createTree()
	{
		return handlerTree.get();
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return TITLE;
	}

	@Override
	protected void doContribute(MyPagesContributeSection section, SectionInfo info, ItemDefinition collection)
	{
		section.contribute(info, collection);
	}

	@Override
	protected void doEdit(MyPagesContributeSection section, SectionInfo info, ItemId id)
	{
		section.edit(info, id, false);
	}

	@Override
	protected void doAddCrumbs(MyPagesContributeSection section, SectionInfo info, Decorations decorations,
		Breadcrumbs crumbs)
	{
		decorations.setContentBodyClass("mycontent-actions-layout");
		decorations.setMenuMode(MenuMode.COLLAPSED);

		section.addCrumbs(info, decorations, crumbs);
	}

	@Override
	public Class<MyPagesContributeSection> getContributeSectionClass()
	{
		return MyPagesContributeSection.class;
	}

	@Override
	public boolean isRawFiles()
	{
		return false;
	}

	@Override
	public HtmlLinkState decorate(SectionInfo info, StandardItemListEntry itemEntry)
	{
		Item item = itemEntry.getItem();
		UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);
		List<HtmlLinkState> pages = new ArrayList<HtmlLinkState>();
		List<HtmlAttachment> htmls = attachments.getList(AttachmentType.HTML);
		NewDefaultViewableItem viewableItem = viewableItemFactory.createNewViewableItem(item.getItemId());
		for( HtmlAttachment html : htmls )
		{
			ViewableResource viewable = attachmentResourceService.getViewableResource(info, viewableItem, html);
			pages.add(processLink(info,
				new HtmlLinkState(new TextLabel(html.getDescription()), viewable.createDefaultViewerUrl())));
		}
		itemEntry.addDelimitedMetadata(new KeyLabel(RESOURCES.key("result.files")),
			(Object[]) pages.toArray(new HtmlLinkState[pages.size()]));
		return processLink(
			info,
			new HtmlLinkState(itemEntry.getTitleLabel(), urlFactory.createItemUrl(info, viewableItem,
				UrlEncodedString.createFromFilePath(MyPagesConstants.VIEW_PAGES), ViewItemUrl.FLAG_IS_RESOURCE)));
	}
}
