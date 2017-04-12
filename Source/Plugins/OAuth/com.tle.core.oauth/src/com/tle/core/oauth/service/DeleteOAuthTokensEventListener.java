package com.tle.core.oauth.service;

import com.tle.core.events.listeners.ApplicationListener;

public interface DeleteOAuthTokensEventListener extends ApplicationListener
{
	void deleteOAuthTokensEvent(DeleteOAuthTokensEvent event);
}
