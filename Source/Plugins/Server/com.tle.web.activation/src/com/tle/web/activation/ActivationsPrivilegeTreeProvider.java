package com.tle.web.activation;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ActivationsPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public ActivationsPrivilegeTreeProvider()
	{
		super(Type.MANAGEMENT_PAGE, ResourcesService.getResourceHelper(ActivationsPrivilegeTreeProvider.class).key(
			"securitytree.manageactivations"), new SettingsTarget("activations"));
	}
}
