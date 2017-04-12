package com.tle.web.customlinks;

import java.io.Serializable;

public class CustomLinkLabel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	private String url;
	private boolean newWindow;
	private String iconUrl;

	public CustomLinkLabel(String name, String url, boolean newWindow, String iconUrl)
	{
		this.name = name;
		this.url = url;
		this.newWindow = newWindow;
		this.iconUrl = iconUrl;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean isNewWindow()
	{
		return newWindow;
	}

	public void setNewWindow(boolean newWindow)
	{
		this.newWindow = newWindow;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

}