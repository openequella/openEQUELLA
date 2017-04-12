package com.tle.upgrade.upgraders;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.io.FileUtils;
import com.google.common.collect.Lists;
import com.tle.upgrade.FileCopier;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class DestroyBIRTEngine extends AbstractUpgrader
{
	public static final String ID = "DestroyBIRTEngine2";

	private static final Log LOGGER = LogFactory.getLog(DestroyBIRTEngine.class);

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		result.setCanRetry(true);

		final File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
		final File newReportingLogsLocation = new File(new File(tleInstallDir, "logs"), "reporting");

		result.info("Reading mandatory properties");
		final ReportingMandatoryConfigModifier mandatory = new ReportingMandatoryConfigModifier(new File(configFolder,
			PropertyFileModifier.MANDATORY_CONFIG));
		final File workspace = mandatory.getWorkspace();

		if( workspace != null )
		{
			// copy log files into new location
			result.info("Copying old reporting logs folder");
			new FileCopier(new File(workspace, "logs"), newReportingLogsLocation, false).copy();

			// delete reporting folder
			FileUtils.delete(workspace);
			// remove workspace from mandatory
			result.info("Removing reporting workspace setting");
			mandatory.updateProperties();
		}
		else
		{
			result.info("No reporting workspace setting in mandatory config");
		}

		if( !newReportingLogsLocation.exists() )
		{
			newReportingLogsLocation.mkdirs();
		}

		// modify log4j
		result.info("Modifying log4j properties");
		new PropertyFileModifier(new File(configFolder, "learningedge-log4j.properties"))
		{
			@Override
			protected boolean modifyProperties(PropertiesConfiguration props)
			{
				props.addProperty("log4j.appender.REPORT.File", new File(newReportingLogsLocation, "/log.html")
					.getAbsolutePath().replaceAll("\\\\", "/"));
				props.addProperty("log4j.logger.org.eclipse.birt", "INFO, REPORT");
				props.addProperty("log4j.appender.REPORT", "com.dytech.common.log4j.DailySizeRollingAppender");
				props.addProperty("log4j.appender.REPORT.Threshold", "DEBUG");
				props.addProperty("log4j.appender.REPORT.ImmediateFlush", "true");
				props.addProperty("log4j.appender.REPORT.Append", "true");
				props.addProperty("log4j.appender.REPORT.layout", "com.dytech.common.log4j.HTMLLayout3");
				props.addProperty("log4j.appender.REPORT.layout.title", "EQUELLA Reporting Logs");
				return true;
			}
		}.updateProperties();
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public List<UpgradeDepends> getDepends()
	{
		UpgradeDepends dep = new UpgradeDepends(UpgradeBIRTEngine.ID);
		dep.setObsoletes(true);
		UpgradeDepends dep2 = new UpgradeDepends(ModifyBirtConfig.ID);
		dep2.setObsoletes(true);
		UpgradeDepends dep3 = new UpgradeDepends(DestroyBIRTEngineOld.ID);
		dep3.setObsoletes(true);
		return Lists.newArrayList(dep, dep2, dep3);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	/**
	 * Test only
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		DestroyBIRTEngine o = new DestroyBIRTEngine();
		UpgradeResult r = new UpgradeResult(LOGGER);
		o.upgrade(r, new File("C:\\equella\\FiveOh"));
	}

	private static class ReportingMandatoryConfigModifier extends PropertyFileModifier
	{
		public ReportingMandatoryConfigModifier(File file) throws ConfigurationException
		{
			super(file);

		}

		@Override
		protected boolean modifyProperties(PropertiesConfiguration props)
		{
			props.clearProperty("reporting.workspace.location");
			return true;
		}

		public File getWorkspace()
		{
			final String ws = props.getString("reporting.workspace.location", null);
			if( ws != null )
			{
				return new File(ws);
			}
			return null;
		}
	}
}
