package com.tle.core.payment.storefront.privileges;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class StoreFrontSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public StoreFrontSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(StoreFrontSettingsPrivilegeTreeProvider.class)
			.key("securitytree.storefrontsettings"), new SettingsTarget("storefrontsettings"));
	}
}