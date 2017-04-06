package com.tle.web.userscripts.privileges;

import javax.inject.Inject;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class UserScriptsPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<UserScript>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(UserScriptsPrivilegeTreeProvider.class);

	@Inject
	protected UserScriptsPrivilegeTreeProvider(UserScriptsService scriptsService)
	{
		super(scriptsService, Node.ALL_USER_SCRIPTS, RESOURCES.key("securitytree.alluserscripts"), Node.USER_SCRIPTS,
			RESOURCES.key("securitytree.targeralluserscripts"));
	}

	@Override
	protected UserScript createEntity()
	{
		return new UserScript();
	}
}
