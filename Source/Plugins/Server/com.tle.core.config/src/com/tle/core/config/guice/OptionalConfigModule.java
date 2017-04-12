package com.tle.core.config.guice;

@SuppressWarnings("nls")
public abstract class OptionalConfigModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/optional-config.properties";
	}
}
