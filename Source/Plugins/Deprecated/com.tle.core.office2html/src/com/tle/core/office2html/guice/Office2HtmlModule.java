package com.tle.core.office2html.guice;

import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.OptionalConfigModule;

/**
 * @author Aaron
 *
 */
public class Office2HtmlModule extends MandatoryConfigModule
{
	@Override
	protected void configure()
	{
		bindProp("java.home");
		install(new Office2HtmlOptionalConfigModule());
	}

	public static class Office2HtmlOptionalConfigModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindBoolean("conversionService.disableConversion");
			bindProp("conversionService.conversionServicePath");
		}
	}
}
