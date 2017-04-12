package com.tle.upgrade.upgraders;

import java.io.File;
import java.net.URL;

import com.tle.common.util.ExecUtils;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.UpgradeMain;
import com.tle.upgrade.UpgradeResult;

public class UpdateManagerJar extends AbstractUpgrader
{
	private static final String MANAGERJAR = "/manager/manager.jar"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public String getId()
	{
		return "UpgradeManager-r" + UpgradeMain.getCommit();
	}

	@Override
	public boolean isRunOnInstall()
	{
		return true;
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@SuppressWarnings("nls")
	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		URL managerUrl = getClass().getResource(MANAGERJAR);
		if( managerUrl == null )
		{
			return;
		}
		String commit = UpgradeMain.getCommit();
		final File newJarFile = new File(tleInstallDir, "manager/manager-r" + commit + ".jar");

		File managerConfig = new File(tleInstallDir, "manager/manager.conf");
		if( managerConfig.exists() )
		{
			LineFileModifier modifier = new LineFileModifier(managerConfig, result)
			{
				@Override
				protected String processLine(String line)
				{
					return line.replaceAll("manager.*\\.jar", newJarFile.getName());
				}
			};
			modifier.update();
		}

		String config = ExecUtils.determinePlatform().startsWith(ExecUtils.PLATFORM_WIN) ? "manager/manager-config.bat"
			: "manager/manager-config.sh";

		File file = new File(tleInstallDir, config);
		if( file.exists() )
		{
			new LineFileModifier(file, result)
			{
				@Override
				protected String processLine(String line)
				{
					return line.replaceAll("manager.*\\.jar", newJarFile.getName());
				}
			}.update();
		}
		copyResource(MANAGERJAR, newJarFile);
	}
}
