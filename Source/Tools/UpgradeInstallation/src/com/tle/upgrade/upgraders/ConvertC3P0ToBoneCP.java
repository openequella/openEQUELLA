package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

@Deprecated
@SuppressWarnings("nls")
public class ConvertC3P0ToBoneCP extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "ConvertC3P0ToBoneCP";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		obsoleteError();
	}
}
