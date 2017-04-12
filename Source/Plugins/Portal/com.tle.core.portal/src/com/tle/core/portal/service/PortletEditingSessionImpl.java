package com.tle.core.portal.service;

import com.tle.common.EntityPack;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

/**
 * @author aholland
 */
public class PortletEditingSessionImpl extends EntityEditingSessionImpl<PortletEditingBean, Portlet>
	implements
		PortletEditingSession
{
	private static final long serialVersionUID = 1L;

	public PortletEditingSessionImpl(String sessionId, EntityPack<Portlet> pack, PortletEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
