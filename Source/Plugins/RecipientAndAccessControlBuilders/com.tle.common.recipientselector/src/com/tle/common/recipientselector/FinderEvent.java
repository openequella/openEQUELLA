package com.tle.common.recipientselector;

/**
 * @author Nicholas Read
 */
public class FinderEvent
{
	private UserGroupRoleFinder source;
	private int selectionCount;

	public FinderEvent()
	{
		super();
	}

	public UserGroupRoleFinder getSource()
	{
		return source;
	}

	public void setSource(UserGroupRoleFinder source)
	{
		this.source = source;
	}

	public int getSelectionCount()
	{
		return selectionCount;
	}

	public void setSelectionCount(int selectionCount)
	{
		this.selectionCount = selectionCount;
	}
}
