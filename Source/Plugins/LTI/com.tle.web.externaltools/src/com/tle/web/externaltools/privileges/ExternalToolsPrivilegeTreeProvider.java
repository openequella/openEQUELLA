package com.tle.web.externaltools.privileges;

import javax.inject.Inject;

import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
public class ExternalToolsPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<ExternalTool>
{
	private static PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ExternalToolsPrivilegeTreeProvider.class);
	
	@SuppressWarnings("nls")
	@Inject
	protected ExternalToolsPrivilegeTreeProvider(ExternalToolsService service)
	{
		super(service, Node.ALL_EXTERNAL_TOOLS, resources.key("securitytree.alltools"), Node.EXTERNAL_TOOL, resources
			.key("securitytree.targetalltools"));
	}

	@Override
	protected ExternalTool createEntity()
	{
		return new ExternalTool();
	}
}

