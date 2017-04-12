package com.tle.core.qti.parse.v1x;

import java.io.Serializable;

/**
 * Represents an image or video etc to make up a QTIMaterial
 * 
 * @author will
 */
public class QTIMaterialMedia implements QTIMaterialElement, Serializable
{
	private static final long serialVersionUID = 1L;
	private String url;
	private QTIMaterialMediaType type;

	public QTIMaterialMedia(String url, QTIMaterialMediaType type)
	{
		this.url = url;
		this.type = type;
	}

	public void setType(QTIMaterialMediaType type)
	{
		this.type = type;
	}

	public QTIMaterialMediaType getType()
	{
		return type;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	@SuppressWarnings("nls")
	@Override
	public String getHtml()
	{
		String html = "";
		switch( type )
		{
			case IMAGE:
				html = "<img src=\"" + url + "\">";
				break;
			case EMBED:
				html = "<object><param name=\"src\" value=\"" + url + "\"><embed src=\"" + url + "\"></embed></object>";
				break;
		}
		return html;
	}
}