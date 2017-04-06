package com.tle.core.config.guice;

@SuppressWarnings("nls")
public abstract class MandatoryConfigModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/mandatory-config.properties";
	}
}
