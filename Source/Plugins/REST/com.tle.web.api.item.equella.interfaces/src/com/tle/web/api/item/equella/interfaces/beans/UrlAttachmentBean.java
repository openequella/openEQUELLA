package com.tle.web.api.item.equella.interfaces.beans;

public class UrlAttachmentBean extends EquellaAttachmentBean
{
	private static final String TYPE = "link";

	private String url;
	private boolean disabled;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	@Override
	public String getRawAttachmentType()
	{
		return TYPE;
	}
}
