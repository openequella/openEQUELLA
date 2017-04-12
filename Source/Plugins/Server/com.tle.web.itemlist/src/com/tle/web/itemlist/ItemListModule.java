package com.tle.web.itemlist;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ItemListEntryFactoryExtension;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ItemListModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		install(new Trackers());
	}

	private static class Trackers extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(ItemlikeListEntryExtension.class, "itemListExtension", "bean").orderByParameter("order");
			bindTracker(ItemListEntryFactoryExtension.class, "itemListFactoryExtension", "bean");
		}
	}
}
