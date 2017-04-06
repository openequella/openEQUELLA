package com.tle.web.manualdatafixes;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class ManualDataFixesPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ManualDataFixesPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(ManualDataFixesSettingsSection.class).key(
			"securitytree.mdf"), new SettingsTarget("manualdatafixes"));
	}
}
