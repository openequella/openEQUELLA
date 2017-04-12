package com.tle.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.dytech.edge.common.Constants;

public class EquellaConfig
{
	private static final String MANDATORY_CONFIG_PROPERTIES = "mandatory-config.properties"; //$NON-NLS-1$

	protected final File installDir;
	protected final File learningEdgeConfigDir;
	protected final File javaBin;
	protected final File managerDir;

	public EquellaConfig(File installDir)
	{
		this.installDir = installDir;
		learningEdgeConfigDir = new File(installDir, Constants.LEARNINGEDGE_CONFIG_FOLDER);
		managerDir = new File(installDir, Constants.MANAGER_FOLDER);

		try( InputStream propFile = new FileInputStream(new File(learningEdgeConfigDir, MANDATORY_CONFIG_PROPERTIES)) )
		{
			Properties props = new Properties();
			props.load(propFile);
			String javaHome = props.getProperty("java.home"); //$NON-NLS-1$
			javaBin = ExecUtils.findExe(new File(javaHome, "bin/java")); //$NON-NLS-1$
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public File getInstallDir()
	{
		return installDir;
	}

	public File getConfigDir()
	{
		return learningEdgeConfigDir;
	}

	public File getJavaBin()
	{
		return javaBin;
	}

	public File getManagerDir()
	{
		return managerDir;
	}
}
