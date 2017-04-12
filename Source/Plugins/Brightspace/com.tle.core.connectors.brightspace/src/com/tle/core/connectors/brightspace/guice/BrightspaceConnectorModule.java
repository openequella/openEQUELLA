package com.tle.core.connectors.brightspace.guice;

import com.tle.core.config.guice.OptionalConfigModule;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
public class BrightspaceConnectorModule extends OptionalConfigModule
{
	private static final String LE_VERSION = "1.7";
	private static final String LP_VERSION = "1.6";

	@Override
	protected void configure()
	{
		bindProp("brightspace.api.le.version", LE_VERSION);
		bindProp("brightspace.api.lp.version", LP_VERSION);
	}
}
