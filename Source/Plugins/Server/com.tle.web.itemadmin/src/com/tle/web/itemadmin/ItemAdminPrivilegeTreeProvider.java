package com.tle.web.itemadmin;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ItemAdminPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ItemAdminPrivilegeTreeProvider()
	{
		super(Type.MANAGEMENT_PAGE, ResourcesService.getResourceHelper(ItemAdminPrivilegeTreeProvider.class).key(
			"securitytree.itemadmin"), new SettingsTarget("itemadmin"));
	}
}
