package com.tle.upgrade.upgraders;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;

@SuppressWarnings("nls")
public class UpdateLog4jConfigForTomcatLog extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "Log4jTomcatConfig";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		final File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
		final File tomcatLogsLocation = new File(new File(tleInstallDir, "logs"), "tomcat");

		if( !tomcatLogsLocation.exists() )
		{
			tomcatLogsLocation.mkdirs();
		}

		// update log4j
		result.info("updating log4j properties");
		new PropertyFileModifier(new File(configFolder, "learningedge-log4j.properties"))
		{
			@Override
			protected boolean modifyProperties(PropertiesConfiguration props)
			{
				if( !props.containsKey("log4j.logger.TomcatLog") )
				{
					props.addProperty("log4j.logger.TomcatLog", "INFO, TOMCAT");
					props.addProperty("log4j.appender.TOMCAT", "com.tle.core.equella.runner.DailySizeRollingAppender");
					props.addProperty("log4j.appender.TOMCAT.File", new File(tomcatLogsLocation, "tomcat.html")
						.getAbsolutePath().replaceAll("\\\\", "/"));
					props.addProperty("log4j.appender.TOMCAT.Threshold", "DEBUG");
					props.addProperty("log4j.appender.TOMCAT.ImmediateFlush", "true");
					props.addProperty("log4j.appender.TOMCAT.Append", "true");
					props.addProperty("log4j.appender.TOMCAT.layout", "com.tle.core.equella.runner.HTMLLayout3");
					props.addProperty("log4j.appender.TOMCAT.layout.title", "Tomcat Logs");
					return true;
				}
				return false;
			}
		}.updateProperties();
	}

}
