package com.tle.core.oauth.service.impl;

import com.tle.common.EntityPack;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.entity.service.impl.EntityEditingSessionImpl;
import com.tle.core.oauth.service.OAuthClientEditingBean;
import com.tle.core.oauth.service.OAuthClientEditingSession;

/**
 * @author aholland
 */
public class OAuthClientEditingSessionImpl extends EntityEditingSessionImpl<OAuthClientEditingBean, OAuthClient>
	implements
		OAuthClientEditingSession
{
	private static final long serialVersionUID = 1L;

	public OAuthClientEditingSessionImpl(String sessionId, EntityPack<OAuthClient> pack, OAuthClientEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
