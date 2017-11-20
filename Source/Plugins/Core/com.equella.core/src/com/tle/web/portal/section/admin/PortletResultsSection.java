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

package com.tle.web.portal.section.admin;

import javax.inject.Inject;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.portal.service.PortletSearch;
import com.tle.core.portal.service.PortletSearchResults;
import com.tle.core.portal.service.PortletService;
import com.tle.web.portal.section.admin.PortletResultsSection.PortletResultsModel;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class PortletResultsSection
	extends
		AbstractSearchResultsSection<PortletListItem, PortletSearchEvent, PortletSearchResultEvent, PortletResultsModel>
{
	@PlugKey("admin.list.confirm.delete")
	private static Confirm DELETE_CONFIRM;
	@PlugKey("admin.list.receipt.delete")
	private static Label LABEL_DELETE_RECEIPT;

	@Inject
	private PortletService portletService;
	@Inject
	private PortletWebService portletWebService;
	@Inject
	private PortletList portletList;
	@Inject
	private PortletListItemFactory factory;
	@Inject
	private ReceiptService receiptService;

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractQuerySection<?, ?> querySection;
	@TreeLookup
	private RootPortletAdminSection rootPortletAdmin;

	public PortletList getPortletList()
	{
		return portletList;
	}

	@EventHandlerMethod
	public void editPortlet(SectionInfo info, String portletUuid)
	{
		portletWebService.editPortlet(info, portletUuid, true);
	}

	@EventHandlerMethod
	public void deletePortlet(SectionInfo info, String portletUuid)
	{
		portletWebService.delete(info, portletUuid);
		receiptService.setReceipt(LABEL_DELETE_RECEIPT);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(portletList, id);
		factory.register(id, tree);
	}

	@EventHandlerMethod
	public void reset(SectionInfo info)
	{
		paging.resetToFirst(info);
	}

	@Override
	public Class<PortletResultsModel> getModelClass()
	{
		return PortletResultsModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pr";
	}

	public static class PortletResultsModel extends AbstractSearchResultsSection.SearchResultsModel
	{
		private boolean showSuggestions;

		public boolean isShowSuggestions()
		{
			return showSuggestions;
		}

		public void setShowSuggestions(boolean showSuggestions)
		{
			this.showSuggestions = showSuggestions;
		}
	}

	@Override
	public void processResults(SectionInfo info, PortletSearchResultEvent results)
	{
		for( Portlet portlet : results.getResults().getResults() )
		{
			portletList.addListItem(info, factory.createPortletListItem(info, portlet,
				events.getNamedHandler("editPortlet", portlet.getUuid()),
				events.getNamedHandler("deletePortlet", portlet.getUuid()).addValidator(DELETE_CONFIRM)));
		}
	}

	@Override
	protected PortletSearchResultEvent createResultsEvent(SectionInfo info, PortletSearchEvent searchEvent)
	{
		int count = portletService.countFromFilters(searchEvent.getUnfilteredSearch());
		PortletSearchResults results = portletService.searchPortlets(searchEvent.getSearch(), searchEvent.getOffset(),
			searchEvent.getCount());

		return new PortletSearchResultEvent(results, searchEvent, count - results.getAvailable());
	}

	@Override
	public PortletSearchEvent createSearchEvent(SectionInfo info)
	{
		PortletSearch search = new PortletSearch();
		PortletSearch unfiltered = new PortletSearch();
		search.setQuery(querySection.getQueryField().getValue(info));
		unfiltered.setQuery(querySection.getQueryField().getValue(info));
		return new PortletSearchEvent(rootPortletAdmin, search, unfiltered);
	}

	@Override
	public PortletList getItemList(SectionInfo info)
	{
		return portletList;
	}
}
