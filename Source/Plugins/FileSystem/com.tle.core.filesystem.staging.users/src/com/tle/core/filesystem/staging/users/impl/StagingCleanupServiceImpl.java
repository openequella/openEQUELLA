package com.tle.core.filesystem.staging.users.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.filesystem.staging.users.StagingCleanupService;
import com.tle.core.guice.Bind;

@Bind(StagingCleanupService.class)
@Singleton
public class StagingCleanupServiceImpl implements StagingCleanupService, UserSessionLogoutListener
{
	@Inject
	private StagingService stagingService;

	@Override
	public void userSessionDestroyedEvent(final UserSessionLogoutEvent event)
	{
		stagingService.removeAllStagingAreas(event.getSessionId());
	}
}
