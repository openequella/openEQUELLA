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

package com.tle.mycontent;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author aholland
 */
public abstract class SimpleContentHandler<S extends ContentHandlerSection> implements ContentHandler
{
	private SectionTree tree;

	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private SelectionService selectionService;

	protected synchronized SectionTree getTree()
	{
		if( tree == null )
		{
			tree = createTree();
		}
		return tree;
	}

	protected abstract SectionTree createTree();

	@Override
	public void contribute(SectionInfo info, ItemDefinition collection)
	{
		addTrees(info, false);
		S contributeSection = getTree().lookupSection(getContributeSectionClass(), null);
		doContribute(contributeSection, info, collection);
	}

	@Override
	public void addTrees(SectionInfo info, boolean parameters)
	{
		if( !info.containsId(getTree().getRootId()) )
		{
			MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
			minfo.addTreeToBottom(tree, parameters);
		}
	}

	@Override
	public boolean canEdit(SectionInfo info, ItemId id)
	{
		return true;
	}

	@Override
	public void edit(SectionInfo info, ItemId id)
	{
		addTrees(info, false);
		S contributeSection = getTree().lookupSection(getContributeSectionClass(), null);
		doEdit(contributeSection, info, id);
	}

	@Override
	public void addCrumbs(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		S contributeSection = getTree().lookupSection(getContributeSectionClass(), null);
		doAddCrumbs(contributeSection, info, decorations, crumbs);
	}

	protected abstract void doContribute(S section, SectionInfo info, ItemDefinition collection);

	protected abstract void doEdit(S section, SectionInfo info, ItemId id);

	protected abstract void doAddCrumbs(S section, SectionInfo info, Decorations decorations, Breadcrumbs crumbs);

	@Override
	public SectionRenderable render(RenderContext info)
	{
		S contributeSection = getTree().lookupSection(getContributeSectionClass(), null);
		return SectionUtils.renderSection(info, contributeSection);
	}

	@Override
	public List<HtmlComponentState> getMajorActions(RenderContext context)
	{
		S contributeSection = getTree().lookupSection(getContributeSectionClass(), null);
		return contributeSection.getMajorActions(context);
	}

	@Override
	public List<HtmlComponentState> getMinorActions(RenderContext context)
	{
		S contributeSection = getTree().lookupSection(getContributeSectionClass(), null);
		return contributeSection.getMinorActions(context);
	}

	@Override
	public HtmlLinkState decorate(SectionInfo info, StandardItemListEntry itemEntry)
	{
		Item item = itemEntry.getItem();
		if( !item.getAttachments().isEmpty() )
		{
			Attachment attachment = item.getAttachments().get(0);
			NewDefaultViewableItem viewableItem = viewableItemFactory.createNewViewableItem(item.getItemId());
			ViewableResource viewable = attachmentResourceService.getViewableResource(info, viewableItem, attachment);

			return processLink(info, new HtmlLinkState(itemEntry.getTitleLabel(), viewable.createCanonicalUrl()));
		}
		return null;
	}

	@SuppressWarnings("nls")
	protected HtmlLinkState processLink(SectionInfo info, HtmlLinkState link)
	{
		if( selectionService.getCurrentSession(info) != null )
		{
			link.setTarget("_blank");
		}
		return link;
	}

	public abstract Class<S> getContributeSectionClass();
}
