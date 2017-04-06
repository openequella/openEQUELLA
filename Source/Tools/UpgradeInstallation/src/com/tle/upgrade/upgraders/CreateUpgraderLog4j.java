package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class CreateUpgraderLog4j extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "CreateUpgraderLog4j";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		final File out = new File(tleInstallDir, "manager/upgrader-log4j.properties");
		if( !out.exists() )
		{
			copyResource("upgrader-log4j.properties", out);
		}
	}
}
