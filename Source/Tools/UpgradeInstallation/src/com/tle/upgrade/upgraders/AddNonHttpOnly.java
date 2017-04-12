package com.tle.upgrade.upgraders;

import java.io.File;

import com.tle.upgrade.UpgradeResult;

/**
 * Obsolete
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Deprecated
public class AddNonHttpOnly extends AbstractUpgrader
{
	public static final String ID = "AddNonHttpOnly";

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		// File tomcatConfFolder = new File(new File(tleInstallDir, "tomcat"),
		// "conf");
		// new LineFileModifier(new File(tomcatConfFolder, "context.xml"),
		// result)
		// {
		// @Override
		// protected String processLine(String line)
		// {
		// if( line.trim().startsWith("<Context") &&
		// !line.contains("useHttpOnly=\"false\"") )
		// {
		// return "<Context useHttpOnly=\"false\">";
		// }
		// return line;
		// }
		// }.update();
		obsoleteError();
	}
}
