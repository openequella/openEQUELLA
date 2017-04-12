package com.tle.web.echo.settings;

import javax.inject.Inject;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.echo.service.EchoService;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class EchoSettingsPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<EchoServer>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(EchoSettingsPrivilegeTreeProvider.class);

	@Inject
	protected EchoSettingsPrivilegeTreeProvider(EchoService echoService)
	{
		super(echoService, Node.ALL_ECHOS, resources.key("securitytree.echoservers"), Node.ECHO, resources
			.key("securitytree.targetallechoservers"));
	}

	@Override
	protected EchoServer createEntity()
	{
		return new EchoServer();
	}
}