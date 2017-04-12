package com.tle.core.events;

import com.tle.core.events.listeners.UserChangeListener;

/**
 * @author Nicholas Read
 */
public class UserEditEvent extends ApplicationEvent<UserChangeListener>
{
	private static final long serialVersionUID = 1L;

	private final String userID;

	public UserEditEvent(String userID)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.userID = userID;
	}

	public String getUserID()
	{
		return userID;
	}

	@Override
	public Class<UserChangeListener> getListener()
	{
		return UserChangeListener.class;
	}

	@Override
	public void postEvent(UserChangeListener listener)
	{
		listener.userEditedEvent(this);
	}
}
