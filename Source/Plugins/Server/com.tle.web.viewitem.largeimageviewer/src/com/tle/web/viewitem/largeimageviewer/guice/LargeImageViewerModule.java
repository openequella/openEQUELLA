package com.tle.web.viewitem.largeimageviewer.guice;

import com.tle.core.config.guice.PropertiesModule;

@SuppressWarnings("nls")
public class LargeImageViewerModule extends PropertiesModule
{
	@Override
	protected void configure()
	{
		bindProp("tileAfterContribution.mode");
	}

	@Override
	protected String getFilename()
	{
		return "/plugins/com.tle.web.viewitem.largeimageviewer/optional.properties";
	}
}
