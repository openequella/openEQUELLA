package com.tle.core.item.standard.guice;

import com.google.inject.AbstractModule;
import com.tle.core.config.guice.OptionalConfigModule;

public class ItemStandardModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new ItemStandardOptionalConfigModule());
	}

	public static class ItemStandardOptionalConfigModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval");
		}
	}
}