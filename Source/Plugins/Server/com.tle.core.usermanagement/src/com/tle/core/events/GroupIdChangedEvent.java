package com.tle.core.events;

import com.tle.core.events.listeners.GroupChangedListener;

/**
 * @author Nicholas Read
 */
public class GroupIdChangedEvent extends ApplicationEvent<GroupChangedListener>
{
	private static final long serialVersionUID = 1L;

	private final String fromGroupId;
	private final String toGroupId;

	public GroupIdChangedEvent(String fromGroupId, String toGroupId)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.fromGroupId = fromGroupId;
		this.toGroupId = toGroupId;
	}

	public String getFromGroupId()
	{
		return fromGroupId;
	}

	public String getToGroupId()
	{
		return toGroupId;
	}

	@Override
	public Class<GroupChangedListener> getListener()
	{
		return GroupChangedListener.class;
	}

	@Override
	public void postEvent(GroupChangedListener listener)
	{
		listener.groupIdChangedEvent(this);
	}
}
