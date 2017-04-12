package com.tle.freetext.guice;

import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.PropertiesModule;

@SuppressWarnings("nls")
public class FreetextModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/plugins/com.tle.core.freetext/optional.properties";
	}

	@Override
	protected void configure()
	{
		bindInt("freetextIndex.synchroiseMinutes");
		bindProp("freetextIndex.defaultOperator");
		bindBoolean("textExtracter.indexAttachments");
		bindBoolean("textExtracter.indexImsPackages");
		install(new FreetextMandatoryModule());
	}

	public static class FreetextMandatoryModule extends MandatoryConfigModule
	{
		@Override
		protected void configure()
		{
			bindFile("freetext.stopwords.file");
			bindFile("freetext.index.location");
		}
	}
}
