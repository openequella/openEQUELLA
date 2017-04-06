package com.tle.web.portal.standard.guice;

import com.tle.core.config.guice.PropertiesModule;

@SuppressWarnings("nls")
public class PortalOptionsModule extends PropertiesModule
{

	@Override
	protected String getFilename()
	{
		return "/plugins/com.tle.web.portal.standard/optional.properties";
	}

	@Override
	protected void configure()
	{
		bindInt("portalSettings.maxRssResults");
		bindInt("portalSettings.maxRssByteSize");
		bindLong("portalSettings.rssCacheTimeout");
	}

}
