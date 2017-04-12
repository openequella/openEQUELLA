package com.tle.web.viewurl;

import java.util.Set;

import com.tle.web.sections.Bookmark;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;

public abstract class WrappedViewItemResource implements ViewItemResource
{
	protected final ViewItemResource inner;
	protected ViewItemResource topLevel;

	public WrappedViewItemResource(ViewItemResource inner)
	{
		this.inner = inner;
		wrappedBy(this);
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return inner.getViewAuditEntry();
	}

	@Override
	public Bookmark createCanonicalURL()
	{
		return inner.createCanonicalURL();
	}

	@Override
	public ContentStream getContentStream()
	{
		return inner.getContentStream();
	}

	@Override
	public ViewItemViewer getViewer()
	{
		return inner.getViewer();
	}

	@Override
	public String getFileDirectoryPath()
	{
		return inner.getFileDirectoryPath();
	}

	@Override
	public String getFilenameWithoutPath()
	{
		return inner.getFilenameWithoutPath();
	}

	@Override
	public String getFilepath()
	{
		return inner.getFilepath();
	}

	@Override
	public int getForwardCode()
	{
		return inner.getForwardCode();
	}

	@Override
	public Set<String> getPrivileges()
	{
		return inner.getPrivileges();
	}

	@Override
	public ViewableItem getViewableItem()
	{
		return inner.getViewableItem();
	}

	@Override
	public String getMimeType()
	{
		return inner.getMimeType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key)
	{
		return (T) inner.getAttribute(key);
	}

	@Override
	public void setAttribute(Object key, Object value)
	{
		inner.setAttribute(key, value);
	}

	@Override
	public boolean getBooleanAttribute(Object key)
	{
		return inner.getBooleanAttribute(key);
	}

	@Override
	public void wrappedBy(ViewItemResource resource)
	{
		topLevel = resource;
		inner.wrappedBy(resource);
	}

	@Override
	public boolean isPathMapped()
	{
		return inner.isPathMapped();
	}

	@Override
	public String getDefaultViewerId()
	{
		return inner.getDefaultViewerId();
	}

	@Override
	public boolean isRestrictedResource()
	{
		return inner.isRestrictedResource();
	}
}
