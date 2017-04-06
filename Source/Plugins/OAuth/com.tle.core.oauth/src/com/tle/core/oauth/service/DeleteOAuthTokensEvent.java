package com.tle.core.oauth.service;

import java.util.List;

import com.tle.core.events.ApplicationEvent;

public class DeleteOAuthTokensEvent extends ApplicationEvent<DeleteOAuthTokensEventListener>
{
	private static final long serialVersionUID = 1L;

	private final List<String> tokens;

	public DeleteOAuthTokensEvent(List<String> tokens)
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
		this.tokens = tokens;
	}

	public List<String> getTokens()
	{
		return tokens;
	}

	@Override
	public Class<DeleteOAuthTokensEventListener> getListener()
	{
		return DeleteOAuthTokensEventListener.class;
	}

	@Override
	public void postEvent(DeleteOAuthTokensEventListener listener)
	{
		listener.deleteOAuthTokensEvent(this);
	}
}
