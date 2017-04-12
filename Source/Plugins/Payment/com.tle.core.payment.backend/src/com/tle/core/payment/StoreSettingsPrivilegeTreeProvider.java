package com.tle.core.payment;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class StoreSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public StoreSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(StoreSettingsPrivilegeTreeProvider.class).key(
			"securitytree.storesettings"), new SettingsTarget("store"));
	}
}