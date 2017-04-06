package com.tle.upgrade.upgraders;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class RemoveClusterGroupName extends AbstractUpgrader
{
	public static final String ID = "RemoveClusterGroupName";

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
	public void upgrade(final UpgradeResult result, File tleInstallDir) throws Exception
	{
		new PropertyFileModifier(
			new File(new File(tleInstallDir, CONFIG_FOLDER), PropertyFileModifier.MANDATORY_CONFIG))
		{
			@Override
			protected boolean modifyProperties(PropertiesConfiguration props)
			{
				if( props.containsKey("cluster.group.name") )
				{
					result.addLogMessage("Removing cluster.group.name property");
					props.clearProperty("cluster.group.name");
					return true;
				}
				return false;
			}
		}.updateProperties();
	}
}
