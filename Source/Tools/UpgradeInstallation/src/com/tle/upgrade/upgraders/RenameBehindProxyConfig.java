package com.tle.upgrade.upgraders;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;

/**
 * Changes loginService.behindProxy to userService.useXForwardedFor
 */
@SuppressWarnings("nls")
public class RenameBehindProxyConfig extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "RenameBehindProxyConfig";
	}

	@Override
	public List<UpgradeDepends> getDepends()
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		new PropertyFileModifier(new File(new File(tleInstallDir, CONFIG_FOLDER), PropertyFileModifier.OPTIONAL_CONFIG))
		{
			@Override
			protected boolean modifyProperties(PropertiesConfiguration props)
			{
				String v = props.getString("loginService.behindProxy");
				if( v == null )
				{
					return false;
				}

				props.setProperty("userService.useXForwardedFor", v);
				props.clearProperty("loginService.behindProxy");
				return true;
			}
		}.updateProperties();
	}
}
