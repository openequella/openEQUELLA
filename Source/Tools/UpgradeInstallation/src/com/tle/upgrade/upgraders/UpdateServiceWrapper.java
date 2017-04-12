package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

public class UpdateServiceWrapper extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "UpdateServiceWrapper"; //$NON-NLS-1$
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		// nothing
	}
}
