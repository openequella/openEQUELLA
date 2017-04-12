package com.tle.upgrade.upgraders;

import java.io.File;

import com.dytech.common.io.FileUtils;
import com.tle.upgrade.UpgradeResult;

@SuppressWarnings("nls")
public class ConvertBoneCPtoHikariCP extends AbstractUpgrader
{

	@Override
	public String getId()
	{
		return "ConvertBoneCPtoHikariCP";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
		copyResource("data/hikari.properties", configFolder);
		File existingBoneCP = new File(configFolder, "bonecp.properties");
		if( existingBoneCP.exists() )
		{
			FileUtils.delete(existingBoneCP);
		}
	}
}
