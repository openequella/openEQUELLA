package com.tle.core.connectors.canvas.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CanvasExternalToolBean
{
	private String id;
	private String url;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

}
