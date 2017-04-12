package com.tle.core.events;

import com.tle.core.events.listeners.UserChangeListener;

/**
 * @author Nicholas Read
 */
public class UserIdChangedEvent extends ApplicationEvent<UserChangeListener>
{
	private static final long serialVersionUID = 1L;

	private final String fromUserId;
	private final String toUserId;

	public UserIdChangedEvent(String fromUserId, String toUserId)
	{
		super(PostTo.POST_ONLY_TO_SELF);

		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
	}

	public String getFromUserId()
	{
		return fromUserId;
	}

	public String getToUserId()
	{
		return toUserId;
	}

	@Override
	public Class<UserChangeListener> getListener()
	{
		return UserChangeListener.class;
	}

	@Override
	public void postEvent(UserChangeListener listener)
	{
		listener.userIdChangedEvent(this);
	}
}
