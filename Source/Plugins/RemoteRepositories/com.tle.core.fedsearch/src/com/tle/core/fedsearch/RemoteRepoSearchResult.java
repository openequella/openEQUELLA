/*
 * Created on Jan 21, 2004
 */
package com.tle.core.fedsearch;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jmaginnis
 */
public class RemoteRepoSearchResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final int index;
	private String title;
	private String description;
	private String url;
	private String uuid;
	private Date publishedDate;

	public RemoteRepoSearchResult(int index)
	{
		this.index = index;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getIndex()
	{
		return index;
	}

	public Date getPublishedDate()
	{
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate)
	{
		this.publishedDate = publishedDate;
	}
}
