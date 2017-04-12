package com.tle.core.events;

import com.tle.core.events.listeners.GroupChangedListener;

/**
 * @author Nicholas Read
 */
public class GroupDeletedEvent extends ApplicationEvent<GroupChangedListener>
{
	private static final long serialVersionUID = 1L;
	private String groupID;

	public GroupDeletedEvent(String groupID)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.groupID = groupID;
	}

	public String getGroupID()
	{
		return groupID;
	}

	@Override
	public Class<GroupChangedListener> getListener()
	{
		return GroupChangedListener.class;
	}

	@Override
	public void postEvent(GroupChangedListener listener)
	{
		listener.groupDeletedEvent(this);
	}
}
