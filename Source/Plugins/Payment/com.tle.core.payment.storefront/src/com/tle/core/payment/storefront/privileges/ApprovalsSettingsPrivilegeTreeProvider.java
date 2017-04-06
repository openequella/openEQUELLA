package com.tle.core.payment.storefront.privileges;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class ApprovalsSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ApprovalsSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(ApprovalsSettingsPrivilegeTreeProvider.class)
			.key("securitytree.approvalssettings"), new SettingsTarget("approvals"));
	}
}