package com.tle.web.google.analytics;

import com.google.inject.Singleton;
import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class GoogleAnalyticsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public GoogleAnalyticsPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, ResourcesService.getResourceHelper(GoogleAnalyticsPrivilegeTreeProvider.class).key(
			"securitytree.googleanalytics"), new SettingsTarget("googleanalytics"));
	}
}
