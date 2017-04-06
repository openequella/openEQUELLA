package com.tle.web.google.api.privileges;

import com.google.inject.Singleton;
import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
public class GoogleApiSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public GoogleApiSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(GoogleApiSettingsPrivilegeTreeProvider.class)
			.key("securitytree.googleapi"), new SettingsTarget("googleapi"));
	}
}
