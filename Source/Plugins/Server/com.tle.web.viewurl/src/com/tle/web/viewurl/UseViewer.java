package com.tle.web.viewurl;

public class UseViewer extends WrappedViewItemResource
{
	private ViewItemViewer viewer;

	public UseViewer(ViewItemResource inner, ViewItemViewer viewer)
	{
		super(inner);
		this.viewer = viewer;
	}

	@Override
	public ViewItemViewer getViewer()
	{
		return viewer;
	}

	@Override
	public boolean isPathMapped()
	{
		return false;
	}
}
