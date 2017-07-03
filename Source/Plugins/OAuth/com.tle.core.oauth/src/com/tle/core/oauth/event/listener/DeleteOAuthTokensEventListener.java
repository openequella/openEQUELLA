package com.tle.core.oauth.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.oauth.event.DeleteOAuthTokensEvent;

public interface DeleteOAuthTokensEventListener extends ApplicationListener
{
	void deleteOAuthTokensEvent(DeleteOAuthTokensEvent event);
}
