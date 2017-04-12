package com.tle.core.system.guice;

import com.tle.core.config.guice.PropertiesModule;

@SuppressWarnings("nls")
public class DatabaseSchemaModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/hibernate.properties";
	}

	@Override
	protected void configure()
	{
		bindConnectionProperty("url");
		bindConnectionProperty("username");
		bindConnectionProperty("password");
	}

	private void bindConnectionProperty(String name)
	{
		String val = getProperty("hibernate.connection." + name);
		bindProp("hibernate.connection." + name);
		bindProp("reporting.connection." + name, val);
	}
}
