package com.tle.core.metadata.exiftool.guice;

import com.tle.core.config.guice.OptionalConfigModule;

@SuppressWarnings("nls")
public class ExifToolModule extends OptionalConfigModule
{
	@Override
	protected void configure()
	{
		bindProp("exiftool.path");
	}
}
