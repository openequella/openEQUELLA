package com.tle.web.api.item.equella.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.item.interfaces.beans.AttachmentBean;

/**
 * An EQUELLA- compatible variety of AttachmentBean.
 * 
 * @author larry
 */
@XmlRootElement
public abstract class EquellaAttachmentBean extends AttachmentBean
{
	private boolean restricted;
	private String thumbnail;

	public boolean isRestricted()
	{
		return restricted;
	}

	public void setRestricted(boolean restricted)
	{
		this.restricted = restricted;
	}

	public String getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail(String thumbnail)
	{
		this.thumbnail = thumbnail;
	}
}
