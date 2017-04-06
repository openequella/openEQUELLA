package com.tle.core.oauth.service;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.events.BaseEntityReferencesEvent;

public class OAuthClientReferencesEvent extends BaseEntityReferencesEvent<OAuthClient, OAuthClientReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

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

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
