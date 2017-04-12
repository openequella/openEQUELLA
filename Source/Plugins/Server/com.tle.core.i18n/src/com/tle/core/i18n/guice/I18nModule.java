package com.tle.core.i18n.guice;

import com.google.inject.AbstractModule;
import com.tle.core.config.guice.OptionalConfigModule;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class I18nModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new I18nOptionalConfigModule());
	}

	public static class I18nOptionalConfigModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("timeZone.default");
		}
	}
}
