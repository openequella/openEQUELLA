package com.tle.web.api.item.equella.interfaces.beans;

public class ZipAttachmentBean extends EquellaAttachmentBean
{
	private static final String TYPE = "zip";

	private boolean mapped;
	private String folder;

	public boolean isMapped()
	{
		return mapped;
	}

	public void setMapped(boolean mapped)
	{
		this.mapped = mapped;
	}

	public String getFolder()
	{
		return folder;
	}

	public void setFolder(String folder)
	{
		this.folder = folder;
	}

	@Override
	public String getRawAttachmentType()
	{
		return TYPE;
	}
}
