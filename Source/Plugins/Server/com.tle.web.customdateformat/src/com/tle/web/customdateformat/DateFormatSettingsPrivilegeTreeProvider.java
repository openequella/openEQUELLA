package com.tle.web.customdateformat;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class DateFormatSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	protected DateFormatSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(DateFormatSettingsPrivilegeTreeProvider.class)
			.key("securitytree.dateformatsettings"), new SettingsTarget("dateformat"));
	}
}
