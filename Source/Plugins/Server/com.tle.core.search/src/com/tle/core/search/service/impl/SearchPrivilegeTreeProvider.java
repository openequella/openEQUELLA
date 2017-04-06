package com.tle.core.search.service.impl;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class SearchPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public SearchPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, "com.tle.core.search.securitytree.searchsettings", new SettingsTarget("searching"));
	}
}
