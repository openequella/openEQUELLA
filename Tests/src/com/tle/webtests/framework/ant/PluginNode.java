package com.tle.webtests.framework.ant;

import java.util.List;

public class PluginNode implements Comparable<PluginNode>
{
	private String name;
	private List<String> plugins;
	private List<PluginNode> children;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getPlugins()
	{
		return plugins;
	}

	public void setPlugins(List<String> plugins)
	{
		this.plugins = plugins;
	}

	public List<PluginNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<PluginNode> children)
	{
		this.children = children;
	}

	@Override
	public int compareTo(PluginNode o)
	{
		return name.compareTo(o.getName());
	}
}