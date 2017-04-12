package com.tle.core.oauth.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class OAuthPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<OAuthClient>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(OAuthPrivilegeTreeProvider.class);

	@Inject
	public OAuthPrivilegeTreeProvider(OAuthService oauthService)
	{
		super(oauthService, Node.ALL_OAUTH_CLIENTS, resources.key("securitytree.alloauthclient"), Node.OAUTH_CLIENT,
			resources.key("securitytree.oauthclient"));
	}

	@Override
	protected OAuthClient createEntity()
	{
		return new OAuthClient();
	}
}
