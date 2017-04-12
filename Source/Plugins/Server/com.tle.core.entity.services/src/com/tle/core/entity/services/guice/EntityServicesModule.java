package com.tle.core.entity.services.guice;

import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.services.item.ItemResolverExtension;
import com.tle.core.xslt.ext.Users;

@SuppressWarnings("nls")
public class EntityServicesModule extends MandatoryConfigModule
{
	@Override
	protected void configure()
	{
		bindProp("java.home");
		bindProp("tomcat.location");
		install(new EntityServicesOptionalModule());
		install(new TrackerModule());
		requestStaticInjection(Users.class);
	}

	public static class EntityServicesOptionalModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("conversionService.conversionServicePath");
			bindProp("com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval");
			bindProp("com.tle.core.tasks.RemoveOldAuditLogs.daysBeforeRemoval");
			bindBoolean("conversionService.disableConversion");
		}
	}

	public static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(ItemResolverExtension.class, "itemResolver", "bean").setIdParam("id");
		}
	}
}
