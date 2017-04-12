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
		tracker = new PluginTracker<MyResourcesSubSearch>(pluginService, getClass(), "subsearch", null)
			.setBeanKey("bean");
	}
}
