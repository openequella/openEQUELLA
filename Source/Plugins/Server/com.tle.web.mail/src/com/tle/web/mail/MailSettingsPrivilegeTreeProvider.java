package com.tle.web.mail;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class MailSettingsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public MailSettingsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(MailSettingsPrivilegeTreeProvider.class).key(
			"securitytree.mailsettings"), new SettingsTarget("mail"));
	}
}