package com.tle.core.events;

import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.user.UserState;

/**
 * @author Nicholas Read
 */
public class UserSessionLogoutEvent extends ApplicationEvent<UserSessionLogoutListener> implements UserSessionEvent
{
	private static final long serialVersionUID = 1L;
	private final boolean entireHttpSessionDestroyed;
	private final UserState userState;
	private final String sessionId;

	public UserSessionLogoutEvent(UserState userState, boolean entireHttpSessionDestroyed)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);

		this.userState = userState;
		this.entireHttpSessionDestroyed = entireHttpSessionDestroyed;
		this.sessionId = userState.getSessionID();
	}

	public boolean isEntireHttpSessionDestroyed()
	{
		return entireHttpSessionDestroyed;
	}

	@Override
	public UserState getUserState()
	{
		return userState;
	}

	@Override
	public String getSessionId()
	{
		return sessionId;
	}

	@Override
	public Class<UserSessionLogoutListener> getListener()
	{
		return UserSessionLogoutListener.class;
	}

	@Override
	public void postEvent(UserSessionLogoutListener listener)
	{
		listener.userSessionDestroyedEvent(this);
	}
}
