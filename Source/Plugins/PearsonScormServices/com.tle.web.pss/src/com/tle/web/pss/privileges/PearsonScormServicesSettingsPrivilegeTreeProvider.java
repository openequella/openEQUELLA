package com.tle.web.pss.privileges;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class PearsonScormServicesSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public PearsonScormServicesSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(
			PearsonScormServicesSettingsPrivilegeTreeProvider.class).key("securitytree.pss.settings"),
			new SettingsTarget("pearsonScormServicesSettings"));
	}
}