package com.tle.core.item.guice;

import com.google.inject.AbstractModule;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.item.ItemIdExtension;
import com.tle.core.item.helper.AbstractHelper;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.scripting.WorkflowScriptObjectContributor;
import com.tle.core.item.service.ItemResolverExtension;

/**
 * @author Aaron
 *
 */
public class ItemModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new TrackerModule());
	}

	public static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(ItemResolverExtension.class, "itemResolver", "bean").setIdParam("id");
			bindTracker(ItemIdExtension.class, "itemIdExtension", "bean").setIdParam("id");
			bindTracker(AbstractHelper.class, "itemHelpers", "bean").orderByParameter("order");
			bindTracker(WorkflowOperation.class, "operation", "class").orderByParameter("order");
			bindTracker(WorkflowScriptObjectContributor.class, "scriptObjects", "class").setIdParam("id");
		}
	}
}
