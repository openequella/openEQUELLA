package com.tle.web.sections.equella.converter;

public abstract class AbstractSessionState implements SessionState
{
	private static final long serialVersionUID = 1L;

	protected transient boolean modified;
	protected transient boolean removed;
	protected String bookmark;

	@Override
	public String getBookmarkString()
	{
		return bookmark;
	}

	@Override
	public boolean isModified()
	{
		return modified;
	}

	@Override
	public boolean isNew()
	{
		return bookmark == null;
	}

	@Override
	public boolean isRemoved()
	{
		return removed;
	}

	@Override
	public void setBookmarkString(String id)
	{
		this.bookmark = id;
	}

	@Override
	public String getSessionId()
	{
		return bookmark;
	}

	@Override
	public void synced()
	{
		modified = false;
	}
}
