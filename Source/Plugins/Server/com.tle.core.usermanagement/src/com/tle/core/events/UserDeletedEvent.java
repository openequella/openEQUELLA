package com.tle.core.events;

import com.google.common.base.Preconditions;
import com.tle.core.events.listeners.UserChangeListener;

/**
 * @author Nicholas Read
 */
public class UserDeletedEvent extends ApplicationEvent<UserChangeListener>
{
	private static final long serialVersionUID = 1L;

	private final String userID;

	public UserDeletedEvent(String userID)
	{
		super(PostTo.POST_ONLY_TO_SELF);

		Preconditions.checkNotNull(userID);
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
		listener.userDeletedEvent(this);
	}
}
