package com.tle.admin.harvester.standard;

import com.tle.common.harvester.SHEXHarvesterSettings;

@SuppressWarnings("nls")
public class SHEXPlugin extends AbstractTLFPlugin<SHEXHarvesterSettings>
{
	public SHEXPlugin()
	{
		super(SHEXHarvesterSettings.class);
	}

	@Override
	protected String getPluginsFieldString()
	{
		return "shexplugin.settings";
	}
}
