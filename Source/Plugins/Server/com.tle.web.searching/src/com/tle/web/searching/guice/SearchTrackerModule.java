package com.tle.web.searching.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.searching.SearchTab;
import com.tle.web.searching.VideoPreviewRenderer;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class SearchTrackerModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		final TrackerProvider<SearchTab> tabTracker = bindTracker(SearchTab.class, "searchTab", "bean");
		tabTracker.orderByParameter("order");

		bindTracker(VideoPreviewRenderer.class, "videoPreviewRenderer", "bean");
	}
}
