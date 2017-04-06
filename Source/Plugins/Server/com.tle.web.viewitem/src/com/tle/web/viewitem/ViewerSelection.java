package com.tle.web.viewitem;

import com.tle.web.viewurl.ViewItemViewer;

public class ViewerSelection
{
	private ViewItemViewer viewer;
	private String viewerId;

	public ViewItemViewer getViewer()
	{
		return viewer;
	}

	public void setViewer(ViewItemViewer viewer)
	{
		this.viewer = viewer;
	}

	public String getViewerId()
	{
		return viewerId;
	}

	public void setViewerId(String viewerId)
	{
		this.viewerId = viewerId;
	}

	public boolean isViewerSet()
	{
		return viewerId != null || viewer != null;
	}
}
