/*
 * Created on Nov 24, 2004 For "The Learning Edge"
 */
package com.tle.core.item.service;

import java.io.Serializable;

import com.tle.beans.Institution;

/**
 * @author jmaginnis
 */
public class DownloadMessage implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String url;
	private long itemdef;
	private final String userid;
	private final Institution institution;

	public DownloadMessage(String userid, Institution institution)
	{
		super();
		this.userid = userid;
		this.institution = institution;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public long getItemdef()
	{
		return itemdef;
	}

	public void setItemdef(long itemdef)
	{
		this.itemdef = itemdef;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public String getUserid()
	{
		return userid;
	}

}
