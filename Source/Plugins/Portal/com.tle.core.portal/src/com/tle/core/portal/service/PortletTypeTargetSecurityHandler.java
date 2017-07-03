/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.portal.service;

import java.util.Set;

import javax.inject.Singleton;

import com.tle.common.portal.PortletTypeTarget;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.entity.security.BaseEntitySecurityTargetHandler;
import com.tle.core.guice.Bind;

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
