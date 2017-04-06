package com.tle.admin.harvester.standard;

import com.tle.common.harvester.LORAXHarvesterSettings;

/**
 * Virtually identical to SHEX & MEXPlugin, differing only in string identifiers
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class LORAXPlugin extends AbstractTLFPlugin<LORAXHarvesterSettings>
{
	public LORAXPlugin()
	{
		super(LORAXHarvesterSettings.class);
	}

	@Override
	protected String getPluginsFieldString()
	{
		return "loraxplugin.settings";
	}
}
