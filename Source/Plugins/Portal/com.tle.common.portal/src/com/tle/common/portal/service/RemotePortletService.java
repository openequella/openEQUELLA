package com.tle.common.portal.service;

import java.util.Collection;

import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.remoting.RemoteAbstractEntityService;

/**
 * @author aholland
 */
public interface RemotePortletService extends RemoteAbstractEntityService<Portlet>
{
	Collection<PortletTypeDescriptor> listAllAvailableTypes();
}
