package com.tle.web.controls.itunesu;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class ITunesUAttachmentBean extends EquellaAttachmentBean
{
	private String playUrl;
	private String trackName;

	public String getPlayUrl()
	{
		return playUrl;
	}

	public void setPlayUrl(String playUrl)
	{
		this.playUrl = playUrl;
	}

	public String getTrackName()
	{
		return trackName;
	}

	public void setTrackName(String trackName)
	{
		this.trackName = trackName;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/itunesu";
	}
}
