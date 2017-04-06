package com.tle.web.mimetypes;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class MimeSearchPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public MimeSearchPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(MimeSearchPrivilegeTreeProvider.class).key(
			"securitytree.mimetypes"), new SettingsTarget("mime"));
	}
}
