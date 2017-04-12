package com.tle.core.events;

import com.tle.core.events.listeners.UserSessionLoginListener;
import com.tle.core.user.UserState;

public class UserSessionLoginEvent extends ApplicationEvent<UserSessionLoginListener> implements UserSessionEvent
{
	private static final long serialVersionUID = 1L;

	private final UserState userState;

	public UserSessionLoginEvent(UserState userState)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.userState = userState;
	}

	@Override
	public String getSessionId()
	{
		return userState.getSessionID();
	}

	@Override
	public Class<UserSessionLoginListener> getListener()
	{
		return UserSessionLoginListener.class;
	}

	@Override
	public void postEvent(UserSessionLoginListener listener)
	{
		listener.userSessionCreatedEvent(this);
	}

	@Override
	public UserState getUserState()
	{
		return userState;
	}
}
