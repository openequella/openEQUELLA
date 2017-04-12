package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

@SuppressWarnings("nls")
public class RemoveQuartzPropertiesFile extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "RemoveQuartzPropertiesFile";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public void upgrade(final UpgradeResult result, File tleInstallDir) throws Exception
	{
		new File(new File(tleInstallDir, CONFIG_FOLDER), "quartz.properties").delete();
	}
}
