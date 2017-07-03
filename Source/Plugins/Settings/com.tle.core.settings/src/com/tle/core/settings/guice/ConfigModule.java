package com.tle.core.settings.guice;

import com.tle.core.config.guice.OptionalConfigModule;

public class ConfigModule extends OptionalConfigModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindProp("configurationService.proxyHost");
		bindInt("configurationService.proxyPort");
		bindProp("configurationService.proxyExceptions");
		bindProp("configurationService.proxyUsername");
		bindProp("configurationService.proxyPassword");
	}
}
