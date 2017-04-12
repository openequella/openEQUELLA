package com.tle.web.search.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.search.settings.EditSearchFilterSection;
import com.tle.web.search.settings.SearchSettingsExtension;
import com.tle.web.search.settings.SearchSettingsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class SearchSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bindNamed("/access/searchsettings", node(SearchSettingsSection.class).child(EditSearchFilterSection.class));
		install(new TrackerModule());
	}

	public static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			final TrackerProvider<SearchSettingsExtension> tracker = bindTracker(SearchSettingsExtension.class,
				"searchSetting", "bean");
			tracker.orderByParameter("order");
		}
	}
}
