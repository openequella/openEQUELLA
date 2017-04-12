package com.tle.upgrade.upgraders;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;

/**
 * Obsolete
 * 
 * @author aholland
 */
@Deprecated
@SuppressWarnings("nls")
public class UpgradeToTomcat6_0_32 extends AbstractUpgrader
{
	public static final String ID = "UpgradeTomcat6_0_32";

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
	public List<UpgradeDepends> getDepends()
	{
		UpgradeDepends dep = new UpgradeDepends(UpgradeToTomcat6_0_26.ID);
		dep.setObsoletes(true);
		return Collections.singletonList(dep);
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}
}
