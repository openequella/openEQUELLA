package com.tle.core.workflow.thumbnail.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.workflow.thumbnail.ThumbnailGenerator;

/**
 * @author Aaron
 *
 */
public class ThumbnailModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		TrackerProvider<ThumbnailGenerator> tracker = bindTracker(ThumbnailGenerator.class, "thumbnailGenerator",
			"generator");
		tracker.setIdParam("mimetype");
	}
}
