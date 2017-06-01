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

package com.tle.mycontent.web.section;

import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.service.MyContentService;
import com.tle.mycontent.web.model.MyContentContributeModel;
import com.tle.mycontent.web.selection.SelectionAllowedHandlers;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
public class MyContentContributeSection extends TwoColumnLayout<MyContentContributeModel>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(MyContentContributeSection.class);

	@Inject
	private MyContentService myContentService;
	@Inject
	private SelectionAllowedHandlers allowedHandlers;

	@Override
	@SuppressWarnings("nls")
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setContentBodyClass("mycontent-layout");

		// //add a crumb to the scrapbook page
		// SectionInfo fwd = RootMyResourcesSection.createForward(info);
		// MyResourcesSearchResults resultsSection = fwd
		// .getTreeAttributeForClass(MyResourcesSearchResults.class);
		// SingleSelectionList<MyResourcesSubSearch> searchType =
		// resultsSection.getSearchType();
		// searchType.setSelectedStringValue(fwd, "scrapbook");
		// crumbs.add(new HtmlLinkState(SCRAPBOOK_LINK_LABEL, new
		// InfoBookmark(fwd)));

		MyContentContributeModel model = getModel(info);
		String handlerId = model.getContributeId();
		if( !Check.isEmpty(handlerId) )
		{
			ContentHandler handler = myContentService.getHandlerForId(handlerId);
			handler.addCrumbs(info, decorations, crumbs);
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		MyContentContributeModel model = getModel(info);
		if( model.getContributeId() != null )
		{
			info.getRootRenderContext().setSemiModalId(getSectionId());
		}
	}

	/**
	 * Invoked from MyContentServiceImpl.forwardToContribute
	 * 
	 * @param info
	 * @param handlerId
	 */
	public void contribute(SectionInfo info, String handlerId)
	{
		if( !Check.isEmpty(handlerId) )
		{
			MyContentContributeModel model = getModel(info);
			ContentHandler handler = myContentService.getHandlerForId(handlerId);
			if( handler == null )
			{
				throw new RuntimeException(RESOURCES.getString("contribute.error.nohandler", //$NON-NLS-1$
					handlerId));
			}
			handler.contribute(info, myContentService.getMyContentItemDef());
			if( !info.isRendered() )
			{
				model.setContributeId(handlerId);
			}
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		/** This must not change so that fixed URLs can be used **/
		return "mycon"; //$NON-NLS-1$
	}

	@Override
	public Class<MyContentContributeModel> getModelClass()
	{
		return MyContentContributeModel.class;
	}

	public static class HandlerDisplay
	{
		protected final HtmlLinkState link;

		protected HandlerDisplay(HtmlLinkState link)
		{
			this.link = link;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}
	}

	public void contributionFinished(SectionInfo info)
	{
		MyContentContributeModel model = getModel(info);
		model.setContributeId(null);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void includeHandler(SectionInfo info)
	{
		MyContentContributeModel model = getModel(info);
		String contributeId = model.getContributeId();
		if( contributeId != null )
		{
			ContentHandler handler = myContentService.getHandlerForId(contributeId);
			handler.addTrees(info, true);
		}
	}

	public void edit(SectionInfo info, String handlerId, ItemId itemId)
	{
		MyContentContributeModel model = getModel(info);
		ContentHandler handler = myContentService.getHandlerForId(handlerId);
		handler.edit(info, itemId);
		if( !info.isRendered() )
		{
			model.setContributeId(handlerId);
		}
	}

	public static SectionInfo createForForward(SectionInfo info)
	{
		return info.createForward("/access/mycontent.do"); //$NON-NLS-1$
	}

	public static void forwardToEdit(SectionInfo info, String handlerId, ItemId itemId)
	{
		MyContentContributeSection section = info.lookupSection(MyContentContributeSection.class);
		if( section != null )
		{
			section.edit(info, handlerId, itemId);
		}
		else
		{
			SectionInfo forward = createForForward(info);
			section = forward.lookupSection(MyContentContributeSection.class);
			section.edit(forward, handlerId, itemId);
			if( forward.isRendered() )
			{
				info.setRendered();
			}
			else
			{
				info.forwardAsBookmark(forward);
			}
		}
	}

	protected Set<String> getAllowedHandlers(SectionInfo info)
	{
		return getModel(info).getAllowedHandlers().get(info, allowedHandlers);
	}
}
