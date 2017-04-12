package com.tle.web.customisation;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class ThemePrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ThemePrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(ThemeSettingsSection.class).key(
			"securitytree.theme"), new SettingsTarget("theme"));
	}
}
