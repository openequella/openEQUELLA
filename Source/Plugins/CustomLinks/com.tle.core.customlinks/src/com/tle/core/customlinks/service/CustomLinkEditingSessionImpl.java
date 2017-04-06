package com.tle.core.customlinks.service;

import com.tle.common.EntityPack;
import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

/**
 * @author aholland
 */
public class CustomLinkEditingSessionImpl extends EntityEditingSessionImpl<CustomLinkEditingBean, CustomLink>
	implements
		CustomLinkEditingSession
{
	private static final long serialVersionUID = 1L;

	public CustomLinkEditingSessionImpl(String sessionId, EntityPack<CustomLink> pack, CustomLinkEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
