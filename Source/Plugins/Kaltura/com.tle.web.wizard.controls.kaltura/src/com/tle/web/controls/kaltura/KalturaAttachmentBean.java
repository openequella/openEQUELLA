package com.tle.web.controls.kaltura;

import java.util.Date;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class KalturaAttachmentBean extends EquellaAttachmentBean
{
	private String mediaId;
	private String title;
	private Date uploadedDate;
	private String thumbUrl;
	private String kalturaServer;
	private String tags;
	private long duration;

	@Override
	public String getRawAttachmentType()
	{
		return "custom/kaltura";
	}

	public String getMediaId()
	{
		return mediaId;
	}

	public void setMediaId(String mediaId)
	{
		this.mediaId = mediaId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Date getUploadedDate()
	{
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate)
	{
		this.uploadedDate = uploadedDate;
	}

	public String getThumbUrl()
	{
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl)
	{
		this.thumbUrl = thumbUrl;
	}

	public String getTags()
	{
		return tags;
	}

	public void setTags(String tags)
	{
		this.tags = tags;
	}

	public long getDuration()
	{
		return duration;
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
	}

	public String getKalturaServer()
	{
		return kalturaServer;
	}

	public void setKalturaServer(String kalturaServer)
	{
		this.kalturaServer = kalturaServer;
	}
}
