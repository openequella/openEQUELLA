package com.tle.web.viewitem.treeviewer;

import java.util.List;

public class TreeNode
{
	private String uuid;
	private String name;
	private boolean open;
	private String icon;
	private String url;
	private boolean nokids;
	private List<TreeTab> tabs;
	private List<TreeNode> children;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isOpen()
	{
		return open;
	}

	public void setOpen(boolean open)
	{
		this.open = open;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean isNokids()
	{
		return nokids;
	}

	public void setNokids(boolean nokids)
	{
		this.nokids = nokids;
	}

	public List<TreeTab> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<TreeTab> tabs)
	{
		this.tabs = tabs;
	}

	public List<TreeNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<TreeNode> children)
	{
		this.children = children;
	}
}