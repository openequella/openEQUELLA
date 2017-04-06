package com.tle.core.libav.guice;

import com.tle.core.config.guice.OptionalConfigModule;

public class LibAvModule extends OptionalConfigModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindProp("libav.path");
	}
}
