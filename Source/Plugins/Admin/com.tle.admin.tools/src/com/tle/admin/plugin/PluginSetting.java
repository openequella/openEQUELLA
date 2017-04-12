/*
 * Created on 1/12/2005
 */
package com.tle.admin.plugin;

import org.java.plugin.registry.Extension;

public class PluginSetting
{
	private String pluginClass;
	private String name;
	private String icon;
	private int width;
	private int height;
	private Extension extension;

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPluginClass()
	{
		return pluginClass;
	}

	public void setPluginClass(String pluginClass)
	{
		this.pluginClass = pluginClass;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public Extension getExtension()
	{
		return extension;
	}

	public void setExtension(Extension extension)
	{
		this.extension = extension;
	}

	public PluginSetting()
	{
		width = 0;
		height = 0;
		icon = "";
	}

}
