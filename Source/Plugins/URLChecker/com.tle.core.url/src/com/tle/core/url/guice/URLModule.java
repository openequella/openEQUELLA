package com.tle.core.url.guice;

import com.tle.core.config.guice.OptionalConfigModule;

/**
 * @author Aaron
 *
 */
public class URLModule extends OptionalConfigModule
{
	private static final int TRIES_UNTIL_WARNING = 5;
	private static final int TRIES_UNTIL_DISABLED = 10;

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindInt("urlChecker.triesUntilWarning", TRIES_UNTIL_WARNING);
		bindInt("urlChecker.triesUntilDisabled", TRIES_UNTIL_DISABLED);
	}
}
