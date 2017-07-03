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

package com.tle.web.myresources.portal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.myresources.MyResourcesListModel;
import com.tle.web.myresources.MyResourcesSearchTypeSection;
import com.tle.web.myresources.MyResourcesSubSearch;
import com.tle.web.myresources.MyResourcesSubSubSearch;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class MyResourcesPortletRenderer extends PortletContentRenderer<MyResourcesPortletRenderer.Model>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private MyResourcesListModel listModel;
	@Inject
	private FreeTextService freeTextService;

	@EventFactory
	private EventGenerator events;

	private List<MyResourcesSubSearch> subSearches;

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		List<MyResourcesSubSearch> sss = Lists.newArrayListWithCapacity(subSearches.size());
		for( MyResourcesSubSearch search : subSearches )
		{
			if( search.isShownOnPortal() && search.canView() )
			{
				sss.add(search);
			}
		}

		List<Search> searches = new ArrayList<Search>();
		for( MyResourcesSubSearch search : sss )
		{
			DefaultSearch defSearch = search.createDefaultSearch(context);
			defSearch.setOwner(CurrentUser.getUserID());
			searches.add(defSearch);
			List<MyResourcesSubSubSearch> subSubs = search.getSubSearches();
			if( subSubs != null )
			{
				for( MyResourcesSubSubSearch subSub : subSubs )
				{
					DefaultSearch subSubSearch = subSub.getSearch();
					subSubSearch.setOwner(CurrentUser.getUserID());
					searches.add(subSubSearch);
				}
			}
		}
		int[] counts = freeTextService.countsFromFilters(searches);

		int i = 0;
		List<SearchRow> searchRows = getModel(context).getSearches();
		for( MyResourcesSubSearch search : sss )
		{
			SearchRow parentRow = new SearchRow(new KeyLabel(search.getNameKey()), counts[i++], new HtmlLinkState(
				events.getNamedHandler("execSearch", search.getValue(), -1)));
			searchRows.add(parentRow);
			List<MyResourcesSubSubSearch> subSubs = search.getSubSearches();
			if( !Check.isEmpty(subSubs) )
			{
				parentRow.setHasKids(true);
				int subCount = 0;
				for( MyResourcesSubSubSearch subSub : subSubs )
				{
					SearchRow childRow = new SearchRow(subSub.getName(), counts[i++], new HtmlLinkState(
						events.getNamedHandler("execSearch", search.getValue(), subCount++)));
					childRow.setChild(true);
					searchRows.add(childRow);
				}
			}
		}

		return viewFactory.createResult("portal/myresources.ftl", this); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void execSearch(SectionInfo info, String type, int subType)
	{
		MyResourcesSearchTypeSection.startSubSearch(info, type, subType);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		subSearches = listModel.createSearches();
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "myr"; //$NON-NLS-1$
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private List<SearchRow> searches = new ArrayList<SearchRow>();

		public List<SearchRow> getSearches()
		{
			return searches;
		}
	}

	public static class SearchRow
	{
		private final Label label;
		private boolean hasKids;
		private boolean child;
		private final Label count;
		private final HtmlComponentState link;

		public SearchRow(Label label, int count, HtmlComponentState link)
		{
			this.label = label;
			this.count = new NumberLabel(count);
			this.link = link;
		}

		public Label getLabel()
		{
			return label;
		}

		public Label getCount()
		{
			return count;
		}

		public HtmlComponentState getLink()
		{
			return link;
		}

		public boolean isHasKids()
		{
			return hasKids;
		}

		public void setHasKids(boolean hasKids)
		{
			this.hasKids = hasKids;
		}

		public boolean isChild()
		{
			return child;
		}

		public void setChild(boolean child)
		{
			this.child = child;
		}
	}
}
