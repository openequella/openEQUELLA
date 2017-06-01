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

package com.tle.web.workflow.myresources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemStatus;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Field;
import com.tle.core.guice.Bind;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.myresources.MyResourcesSubSearch;
import com.tle.web.myresources.MyResourcesSubSubSearch;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionTree.DelayedRegistration;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.workflow.tasks.FilterByWorkflowTaskSection;
import com.tle.web.workflow.tasks.WorkflowFromCollectionSection;

@Bind
@SuppressWarnings("nls")
public class ModerationQueueSearch extends AbstractPrototypeSection<Object> implements MyResourcesSubSearch
{
	static
	{
		PluginResourceHandler.init(ModerationQueueSearch.class);
	}

	@Inject
	private ModerationQueueItemList itemList;
	@Inject
	private ModerationQueueSort sort;
	@Inject
	private ModerationQueueStatusFilter statusFilter;
	@Inject
	private FilterByWorkflowTaskSection taskFilter;
	@Inject
	private WorkflowFromCollectionSection workflowSelector;
	private final List<MyResourcesSubSubSearch> subSearches = new ArrayList<MyResourcesSubSubSearch>();

	@PlugKey("search.name")
	private static String KEY_NAME;
	@PlugKey("subsearch.")
	private static String KEY_SUBSEARCH;

	public ModerationQueueSearch()
	{
		subSearches.add(new SubSearch(ItemStatus.MODERATING));
		subSearches.add(new SubSearch(ItemStatus.REVIEW));
		subSearches.add(new SubSearch(ItemStatus.REJECTED));
	}

	@Override
	public String getNameKey()
	{
		return KEY_NAME;
	}

	@Override
	public int getOrder()
	{
		return 400;
	}

	@Override
	public String getValue()
	{
		return "modqueue";
	}

	@Override
	public MyResourcesSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void addExtraMusts(List<List<Field>> musts)
			{
				musts.add(Arrays.asList(new Field(FreeTextQuery.FIELD_MODERATING, "true"), new Field(
					FreeTextQuery.FIELD_ITEMSTATUS, ItemStatus.REJECTED.toString())));
			}

			@Override
			public String getPrivilege()
			{
				return null;
			}
		};
		return search;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		tree.registerInnerSection(itemList, parentId);
		tree.addDelayedRegistration(new DelayedRegistration()
		{
			@Override
			public void register(SectionTree tree)
			{
				String parentId = tree.getPlaceHolder("SEARCH_RESULTS_ACTIONS");
				tree.registerSections(sort, parentId);
				// Can't TreeLookup this?
				FilterByCollectionSection collectionSection = tree.lookupSection(FilterByCollectionSection.class, null);

				// Can't TreeLookup this?
				FilterByItemStatusSection statusSection = tree.lookupSection(FilterByItemStatusSection.class, null);
				tree.registerSections(taskFilter, parentId, collectionSection.getSectionId(), true);
				tree.registerSections(statusFilter, parentId, statusSection.getSectionId(), true);
				tree.registerInnerSection(workflowSelector, parentId);
			}
		});
	}

	@Override
	public void setupFilters(SectionInfo info)
	{
		sort.enable(info);
		statusFilter.enable(info);
	}

	@Override
	public List<MyResourcesSubSubSearch> getSubSearches()
	{
		return subSearches;
	}

	private static DefaultSearch createSearch(ItemStatus itemStatus)
	{
		DefaultSearch search = new DefaultSearch();
		search.setItemStatuses(itemStatus);
		return search;
	}

	public class SubSearch extends MyResourcesSubSubSearch
	{
		private final ItemStatus itemStatus;

		public SubSearch(ItemStatus itemStatus)
		{
			super(new KeyLabel(KEY_SUBSEARCH + itemStatus.name().toLowerCase()), createSearch(itemStatus));
			this.itemStatus = itemStatus;
		}

		@Override
		public void execute(SectionInfo info)
		{
			statusFilter.getItemStatus().setSelectedStringValue(info, itemStatus.name().toLowerCase());
		}
	}

	@Override
	public boolean isShownOnPortal()
	{
		return true;
	}

	@Override
	public boolean canView()
	{
		return true;
	}

	@Override
	public ModerationQueueItemList getCustomItemList()
	{
		return itemList;
	}
}
