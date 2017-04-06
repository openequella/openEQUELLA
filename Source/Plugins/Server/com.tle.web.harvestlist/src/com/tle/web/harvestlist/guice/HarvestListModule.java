package com.tle.web.harvestlist.guice;

import com.tle.core.config.guice.PropertiesModule;

@SuppressWarnings("nls")
public class HarvestListModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/plugins/com.tle.web.harvestlist/optional.properties";
	}

	@Override
	protected void configure()
	{
		bindProp("where");
	}
}
