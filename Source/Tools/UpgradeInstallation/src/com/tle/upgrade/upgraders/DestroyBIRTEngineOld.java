package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

/**
 * Obsolete
 * 
 * @author Aaron
 */
@Deprecated
@SuppressWarnings("nls")
public class DestroyBIRTEngineOld extends AbstractUpgrader
{
	public static final String ID = "DestroyBIRTEngine";

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
