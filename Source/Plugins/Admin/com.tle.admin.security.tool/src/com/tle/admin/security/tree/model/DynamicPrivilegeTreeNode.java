package com.tle.admin.security.tree.model;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.security.remoting.RemotePrivilegeTreeService;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.core.plugins.PluginService;

/**
 * @author Nicholas Read
 */
public class DynamicPrivilegeTreeNode extends AbstractLazyNode
{
	private final SecurityTarget target;
	private final PluginService pluginService;
	private final RemotePrivilegeTreeService privTreeService;

	public DynamicPrivilegeTreeNode(PluginService pluginService, RemotePrivilegeTreeService privTreeService,
		SecurityTarget target)
	{
		super(target.getDisplayName(), target.getTargetType());

		this.target = target;
		this.pluginService = pluginService;
		this.privTreeService = privTreeService;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return target.hasChildTargets();
	}

	@Override
	public Object getTargetObject()
	{
		return target.getTarget();
	}

	@Override
	protected List<SecurityTreeNode> getChildren()
	{
		return getSecurityTargetsAsTreeNodes(pluginService, privTreeService, target);
	}

	public static List<SecurityTreeNode> getSecurityTargetsAsTreeNodes(final PluginService pluginService,
		final RemotePrivilegeTreeService service, final SecurityTarget target)
	{
		return Lists.transform(service.getChildTargets(target), new Function<SecurityTarget, SecurityTreeNode>()
		{
			@Override
			public SecurityTreeNode apply(SecurityTarget input)
			{
				return new DynamicPrivilegeTreeNode(pluginService, service, input);
			}
		});
	}
}
