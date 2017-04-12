package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

/**
 * Obsolete
 * 
 * @author aholland
 */
@Deprecated
@SuppressWarnings("nls")
public class UpgradeToTomcat6_0_26 extends AbstractUpgrader
{
	public static final String ID = "UpgradeTomcat6_0_26";

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		obsoleteError();
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}
}
