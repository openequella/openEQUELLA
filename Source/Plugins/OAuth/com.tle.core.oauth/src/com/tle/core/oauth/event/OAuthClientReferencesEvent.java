package com.tle.core.oauth.event;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.entity.event.BaseEntityReferencesEvent;
import com.tle.core.oauth.event.listener.OAuthClientReferencesListener;

public class OAuthClientReferencesEvent extends BaseEntityReferencesEvent<OAuthClient, OAuthClientReferencesListener>
{
	private static final long serialVersionUID = 1L;

	public OAuthClientReferencesEvent(OAuthClient client)
	{
		super(client);
	}

	@Override
	public Class<OAuthClientReferencesListener> getListener()
	{
		return OAuthClientReferencesListener.class;
	}

	@Override
	public void postEvent(OAuthClientReferencesListener listener)
	{
		listener.addOAuthClientReferencingClasses(entity, referencingClasses);
	}
}
