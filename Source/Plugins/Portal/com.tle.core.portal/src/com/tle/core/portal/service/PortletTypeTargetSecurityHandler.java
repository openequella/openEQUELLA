package com.tle.core.portal.service;

import java.util.Set;

import javax.inject.Singleton;

import com.tle.common.portal.PortletTypeTarget;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.impl.BaseEntitySecurityTargetHandler;

/**
 * This is a "labeller" only. Works on Portlets and PortletTypeTargets
 * 
 * @author Aaron
 */
@Bind
@Singleton
public class PortletTypeTargetSecurityHandler extends BaseEntitySecurityTargetHandler
{
	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		labels.add(getPrimaryLabel(target));
		if( isPortlet(target) )
		{
			labels.add(getTypeLabel((Portlet) target));
		}
	}

	private String getTypeLabel(Portlet portlet)
	{
		return new PortletTypeTarget(portlet.getType()).getTarget();
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		if( isPortlet(target) )
		{
			return super.getPrimaryLabel(target);
		}
		return ((PortletTypeTarget) target).getTarget();
	}

	@Override
	public Object transform(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		throw new UnsupportedOperationException();
	}

	private boolean isPortlet(Object target)
	{
		return target instanceof Portlet;
	}
}
