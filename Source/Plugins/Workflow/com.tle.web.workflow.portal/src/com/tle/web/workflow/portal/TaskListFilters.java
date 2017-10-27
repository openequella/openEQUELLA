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

package com.tle.web.workflow.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Maps;
import com.tle.beans.item.Item;
import com.tle.common.search.DefaultSearch;
import com.tle.common.workflow.TaskFilterCount;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.item.TaskResult;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.workflow.portal.TaskListXML.ItemTask;
import com.tle.web.workflow.portal.TaskListXML.ItemTasks;
import com.tle.web.workflow.portal.TaskListXML.TaskFilters;

@Bind
@Singleton
public class TaskListFilters
{
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private TaskListXML taskListXML;

	@Inject
	private PluginTracker<TaskListExtension> tracker;
	private Map<String, TaskListSubsearch> filterMap = null;

	public synchronized Map<String, TaskListSubsearch> getFilterMap()
	{
		if( filterMap == null || tracker.needsUpdate() )
		{
			Map<String, TaskListSubsearch> newMap = Maps.newLinkedHashMap();
			for( TaskListExtension ext : tracker.getBeanList() )
			{
				for( TaskListSubsearch filter : ext.getTaskFilters() )
				{
					String identifier = filter.getIdentifier();
					newMap.put(identifier, filter);
				}
			}
			filterMap = newMap;
		}
		return filterMap;
	}

	public TaskListSubsearch getFilterForIdentifier(String filter)
	{
		return getFilterMap().get(filter);
	}

	public List<TaskFilterCount> getFilterCounts(boolean ignoreZero)
	{
		return getFilterCounts(ignoreZero, false);
	}

	public List<TaskFilterCount> getFilterCounts(boolean ignoreZero, boolean skipHref)
	{
		List<DefaultSearch> searches = new ArrayList<DefaultSearch>();
		List<TaskFilterCount> filterList = new ArrayList<TaskFilterCount>();
		addFilters(filterList, searches, getFilterMap().values());
		int[] counts = freeTextService.countsFromFilters(searches);
		Iterator<TaskFilterCount> iter = filterList.iterator();
		int i = 0;
		while( iter.hasNext() )
		{
			TaskFilterCount filterCount = iter.next();
			int count = counts[i++];
			if( !ignoreZero || count > 0 )
			{
				filterCount.setCount(count);

				SectionInfo info = setupSearch(null, filterCount.getId());
				if( !skipHref )
				{
					filterCount.setHref(new InfoBookmark(info).getHref());
				}
			}
			else
			{
				iter.remove();
			}
		}
		return filterList;
	}

	public String getFilterCountsXML(boolean ignoreZero)
	{
		List<TaskFilterCount> filterList = getFilterCounts(ignoreZero);
		return taskListXML.toXML(new TaskFilters(filterList));
	}

	private void addFilters(List<TaskFilterCount> filterList, List<DefaultSearch> searches,
		Collection<? extends TaskListSubsearch> filters)
	{
		for( TaskListSubsearch subSearch : filters )
		{
			String id = subSearch.getIdentifier();
			String name = subSearch.getName().getText();
			TaskFilterCount filterCount = new TaskFilterCount(id, name);
			searches.add(subSearch.getSearch());
			if( subSearch.isSecondLevel() )
			{
				filterCount.setParent(subSearch.getParentIdentifier());
			}
			filterList.add(filterCount);
		}
	}

	private SectionInfo setupSearch(SectionInfo info, String filter)
	{
		TaskListSubsearch search = getFilterForIdentifier(filter);
		return search.setupForward(info);
	}

	public Set<String> getFilterNamesSet()
	{
		return getFilterMap().keySet();
	}

	public String[] getFilterNames()
	{
		Set<String> filterNames = getFilterMap().keySet();
		return filterNames.toArray(new String[filterNames.size()]);
	}

	public String getFilterResultsXML(String filterName, int start, int numResults)
	{
		TaskListSubsearch filter = getFilterForIdentifier(filterName);
		DefaultSearch search = filter.getSearch();
		FreetextSearchResults<FreetextResult> results = freeTextService.search(search, start, numResults);
		List<ItemTask> tasks = new ArrayList<ItemTask>();
		int i = 0;
		for( Item item : results.getResults() )
		{
			FreetextResult resultData = results.getResultData(i++);
			String taskUuid = null;
			if( resultData instanceof TaskResult )
			{
				taskUuid = ((TaskResult) resultData).getTaskId();
			}
			tasks.add(new ItemTask(item.getUuid(), item.getVersion(), taskUuid));
		}
		return taskListXML.toXML(new ItemTasks(tasks));
	}

	public void execSearch(SectionInfo info, String filter)
	{
		SectionInfo search = setupSearch(info, filter);
		info.forward(search);
	}

	public Collection<TaskListSubsearch> getFilters()
	{
		return getFilterMap().values();
	}

}
