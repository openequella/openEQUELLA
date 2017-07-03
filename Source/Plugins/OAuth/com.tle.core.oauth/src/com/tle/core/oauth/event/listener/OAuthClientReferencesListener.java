package com.tle.core.oauth.event.listener;

import java.util.List;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface OAuthClientReferencesListener extends ApplicationListener
{
	void addOAuthClientReferencingClasses(OAuthClient client, List<Class<?>> referencingClasses);
}
