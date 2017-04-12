/**
 * 
 */
package com.tle.web.harvesterskipdrmsettings;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class HarvesterSkipDrmSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public HarvesterSkipDrmSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(
			HarvesterSkipDrmSettingsPrivilegeTreeProvider.class).key("securitytree.harvesterskipdrmsettings"),
			new SettingsTarget("harvesterSkipDrmSettings"));
	}
}
