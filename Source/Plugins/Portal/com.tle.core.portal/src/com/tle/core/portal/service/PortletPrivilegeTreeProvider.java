package com.tle.core.portal.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.common.portal.PortletTypeTarget;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PortletPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Portlet>
{
	private static final String PORTLET_TARGET_KEY = ResourcesService.getResourceHelper(
		PortletPrivilegeTreeProvider.class).key("securitytree.targetportlettype");

	private final PortletService portletService;

	@Inject
	public PortletPrivilegeTreeProvider(PortletService portletService)
	{
		super(portletService, Node.ALL_PORTLETS, ResourcesService.getResourceHelper(PortletPrivilegeTreeProvider.class)
			.key("securitytree.allportlets"), Node.PORTLET, ResourcesService.getResourceHelper(
			PortletPrivilegeTreeProvider.class).key("securitytree.targetallportlets"));

		this.portletService = portletService;
	}

	@Override
	public void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target)
	{
		if( target == null )
		{
			super.gatherChildTargets(childTargets, null);
		}
		else if( target.getTargetType() == Node.ALL_PORTLETS )
		{
			for( PortletTypeDescriptor type : portletService.listAllAvailableTypes() )
			{
				childTargets.add(new SecurityTarget(CurrentLocale.get(type.getNameKey()), type.getNode(),
					new PortletTypeTarget(type.getType()), false));
			}
		}
	}

	@Override
	protected void fallbackMapTargetIdToName(TargetId targetId, Map<TargetId, String> results)
	{
		String target = targetId.getTarget();
		if( target.startsWith("P") )
		{
			String id = target.substring(2);
			String nameKey = portletService.mapAllAvailableTypes().get(id).getNameKey();

			results.put(targetId, CurrentLocale.get(PORTLET_TARGET_KEY, CurrentLocale.get(nameKey)));
		}
	}

	@Override
	protected Portlet createEntity()
	{
		return new Portlet();
	}
}
