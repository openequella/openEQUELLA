package com.tle.admin.harvester.standard;

import com.tle.common.harvester.MEXHarvesterSettings;

@SuppressWarnings("nls")
public class MEXPlugin extends AbstractTLFPlugin<MEXHarvesterSettings>
{
	public MEXPlugin()
	{
		super(MEXHarvesterSettings.class);
	}

	@Override
	protected String getPluginsFieldString()
	{
		return "mexplugin.settings";
	}
}
