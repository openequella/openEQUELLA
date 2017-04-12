package com.tle.core.imagemagick.guice;

import com.tle.core.config.guice.PropertiesModule;

public class ImageMagickModule extends PropertiesModule
{

	@SuppressWarnings("nls")
	@Override
	protected String getFilename()
	{
		return "/plugins/com.tle.core.imagemagick/config.properties";
	}

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindProp("imageMagick.path");
	}
}
