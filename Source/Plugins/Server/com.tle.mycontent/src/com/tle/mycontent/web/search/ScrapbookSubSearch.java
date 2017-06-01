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

package com.tle.mycontent.web.search;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.mycontent.service.MyContentService;
import com.tle.mycontent.web.section.ContributeMyContentAction;
import com.tle.mycontent.web.sort.ScrapbookSortSection;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.myresources.MyResourcesSubSearch;
import com.tle.web.myresources.MyResourcesSubSubSearch;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionTree.DelayedRegistration;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.generic.AbstractPrototypeSection;

@Bind
@SuppressWarnings("nls")
public class ScrapbookSubSearch extends AbstractPrototypeSection<ScrapbookSubSearch.Model>
	implements
		MyResourcesSubSearch
{
	@Inject
	private MyContentService myContentService;
	@TreeLookup
	private FilterByItemStatusSection statusSection;
	@TreeLookup
	private FilterByCollectionSection collectionSection;
	@TreeLookup
	private SearchResultsActionsSection resultsActionsSection;

	@Inject
	private ScrapbookSortSection sort;

	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(ScrapbookSubSearch.class);

	@Override
	public String getNameKey()
	{
		return helper.key("searchname"); //$NON-NLS-1$
	}

	@Override
	public String getValue()
	{
		return "scrapbook"; //$NON-NLS-1$
	}

	@Override
	public void register(SectionTree tree, final String parentId)
	{
		tree.registerInnerSection(this, parentId);

		tree.addDelayedRegistration(new DelayedRegistration()
		{
			@Override
			public void register(SectionTree tree)
			{
				tree.registerSections(sort, tree.getPlaceHolder("SEARCH_RESULTS_ACTIONS"));
				Set<String> handlers = myContentService.getContentHandlerIds();
				for( String handlerId : handlers )
				{
					ContributeMyContentAction action = myContentService.createActionForHandler(handlerId);
					tree.registerSections(action, parentId);
				}
			}
		});
	}

	@Override
	public MyResourcesSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(ItemStatus.PERSONAL);
		return search;
	}

	@Override
	public int getOrder()
	{
		return 300;
	}

	@Override
	public AbstractItemList<?, ?> getCustomItemList()
	{
		return null;
	}

	@Override
	public void setupFilters(SectionInfo info)
	{
		getModel(info).setEnabled(true);
		statusSection.disable(info);
		collectionSection.disable(info);
		resultsActionsSection.disableSaveAndShare(info);
		sort.enable(info);
	}

	public boolean isEnabled(SectionInfo info)
	{
		return getModel(info).isEnabled();
	}

	public static class Model
	{
		private boolean enabled;

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	public List<MyResourcesSubSubSearch> getSubSearches()
	{
		return null;
	}

	@Override
	public boolean isShownOnPortal()
	{
		return true;
	}

	@Override
	public boolean canView()
	{
		return myContentService.isMyContentContributionAllowed();
	}
}
