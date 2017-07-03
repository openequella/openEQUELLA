package com.tle.core.collection.guice;

import com.tle.core.collection.extension.CollectionSaveExtension;
import com.tle.core.guice.PluginTrackerModule;

/**
 * @author Aaron
 *
 */
public class CollectionTrackerModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(CollectionSaveExtension.class, "collectionSave", "bean");
	}
}
