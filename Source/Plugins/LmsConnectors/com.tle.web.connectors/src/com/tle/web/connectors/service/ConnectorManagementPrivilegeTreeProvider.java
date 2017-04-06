package com.tle.web.connectors.service;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ConnectorManagementPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ConnectorManagementPrivilegeTreeProvider()
	{
		super(Type.MANAGEMENT_PAGE, ResourcesService.getResourceHelper(ConnectorManagementPrivilegeTreeProvider.class)
			.key("securitytree.manageconnectors"), new SettingsTarget("connectors"));
	}
}
