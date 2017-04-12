package com.tle.web.viewitem.treeviewer;

import java.util.List;

import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.common.NameValue;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.CachedData;
import com.tle.web.viewurl.ViewItemResource;

public class AbstractTreeViewerModel
{
	private String definition;
	private List<NameValue> tabs;
	private final CachedData<ItemNavigationTree> navigationTree = new CachedData<ItemNavigationTree>();
	private ViewItemResource resource;
	private String method;

	@Bookmarked(parameter = "hideNavBar")
	private boolean hideNavBar;
	private boolean hideNavControls;
	private boolean hideTree;

	@Bookmarked
	private String node;

	public String getNode()
	{
		return node;
	}

	public void setNode(String node)
	{
		this.node = node;
	}

	public List<NameValue> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<NameValue> tabs)
	{
		this.tabs = tabs;
	}

	public CachedData<ItemNavigationTree> getNavigationTree()
	{
		return navigationTree;
	}

	public String getDefinition()
	{
		return definition;
	}

	public void setDefinition(String definition)
	{
		this.definition = definition;
	}

	public boolean isHideNavBar()
	{
		return hideNavBar;
	}

	public void setHideNavBar(boolean hideNavBar)
	{
		this.hideNavBar = hideNavBar;
	}

	public boolean isHideNavControls()
	{
		return hideNavControls;
	}

	public void setHideNavControls(boolean hideNavControls)
	{
		this.hideNavControls = hideNavControls;
	}

	public boolean isHideTree()
	{
		return hideTree;
	}

	public void setHideTree(boolean hideTree)
	{
		this.hideTree = hideTree;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public ViewItemResource getResource()
	{
		return resource;
	}

	public void setResource(ViewItemResource resource)
	{
		this.resource = resource;
	}
}