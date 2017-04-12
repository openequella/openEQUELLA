package com.tle.web.viewurl;

import java.util.HashMap;
import java.util.Map;

public class ResourceViewerConfig
{
	private boolean openInNewWindow;
	private String width;
	private String height;
	private boolean thickbox;
	private Map<String, Object> attr = new HashMap<String, Object>();

	public boolean isOpenInNewWindow()
	{
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow)
	{
		this.openInNewWindow = openInNewWindow;
	}

	public String getWidth()
	{
		return width;
	}

	public void setWidth(String width)
	{
		this.width = width;
	}

	public String getHeight()
	{
		return height;
	}

	public void setHeight(String height)
	{
		this.height = height;
	}

	public Map<String, Object> getAttr()
	{
		return attr;
	}

	public void setAttr(Map<String, Object> attrs)
	{
		this.attr = attrs;
	}

	public boolean isThickbox()
	{
		return thickbox;
	}

	public void setThickbox(boolean thickbox)
	{
		this.thickbox = thickbox;
	}

}
