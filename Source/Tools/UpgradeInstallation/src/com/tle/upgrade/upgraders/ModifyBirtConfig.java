package com.tle.upgrade.upgraders;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.tle.upgrade.UpgradeResult;

@SuppressWarnings("nls")
public class ModifyBirtConfig extends AbstractUpgrader
{
	public static final String ID = "ModifyBirtConfig";

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

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
		Properties props = new Properties();
		try( FileInputStream mandatoryStream = new FileInputStream(
			new File(configFolder, "mandatory-config.properties")) )
		{
			props.load(new InputStreamReader(mandatoryStream, "UTF-8"));
			File reportingConfig = new File(props.getProperty("reporting.workspace.location"), "configuration");
			if( !reportingConfig.exists() )
			{
				throw new Exception("Reporting configuration folder could not be found");
			}
			copyResource("data/config.ini", new File(reportingConfig, "config.ini"));
		}
	}
}
