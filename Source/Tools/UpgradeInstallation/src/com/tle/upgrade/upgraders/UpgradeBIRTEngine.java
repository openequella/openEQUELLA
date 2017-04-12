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
public class UpgradeBIRTEngine extends AbstractUpgrader
{
	public static final String ID = "UpgradeBIRT2.6.1";

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

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
}
