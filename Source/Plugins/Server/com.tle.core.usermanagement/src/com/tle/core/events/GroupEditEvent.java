package com.tle.core.events;

import java.util.Set;

import com.tle.core.events.listeners.GroupChangedListener;

/**
 * @author Nicholas Read
 */
public class GroupEditEvent extends ApplicationEvent<GroupChangedListener>
{
	private static final long serialVersionUID = 1L;
	private String groupID;
	private Set<String> newMembers;

	public GroupEditEvent(String groupID, Set<String> newMembers)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.groupID = groupID;
		this.newMembers = newMembers;
	}

	public String getGroupID()
	{
		return groupID;
	}
	
	public Set<String> getNewMembers()
	{
		return newMembers;
	}

	@Override
	public Class<GroupChangedListener> getListener()
	{
		return GroupChangedListener.class;
	}

	@Override
	public void postEvent(GroupChangedListener listener)
	{
		listener.groupEditedEvent(this);
	}
}
