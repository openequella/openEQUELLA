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

package com.tle.web.myresources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.generic.NumberOrderComparator;

@Bind(MyResourcesService.class)
public class MyResourcesServiceImpl implements MyResourcesService
{

	private PluginResourceHelper helper = ResourcesService.getResourceHelper(getClass());
	private PluginTracker<MyResourcesSubSearch> tracker;

	@Override
	public List<MyResourcesSubSearch> listSearches()
	{
		List<MyResourcesSubSearch> subSearches = new ArrayList<MyResourcesSubSearch>();
		subSearches.add(new ItemStatusSubSearch(helper.key("subsearch.published"), "published", 100, ItemStatus.LIVE,
			ItemStatus.REVIEW));
		subSearches.add(new ItemStatusSubSearch(helper.key("subsearch.draft"), "draft", 200, ItemStatus.DRAFT));
		ItemStatusSubSearch archiveSearch = new ItemStatusSubSearch(helper.key("subsearch.archived"), "archived", 500,
			ItemStatus.ARCHIVED);
		archiveSearch.setShownOnPortal(false);
		subSearches.add(archiveSearch);
		subSearches.add(new AllResourcesSubSearch(helper.key("subsearch.all")));
		subSearches.addAll(tracker.getNewBeanList());
		Collections.sort(subSearches, NumberOrderComparator.LOWEST_FIRST);
		return subSearches;
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<MyResourcesSubSearch>(pluginService, "com.tle.web.myresources", "subsearch", null)
			.setBeanKey("bean");
	}
}
