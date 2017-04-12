package com.tle.web.api.item.equella.interfaces.beans;

public abstract class AbstractFileAttachmentBean extends EquellaAttachmentBean
{
	private String filename;
	private long size;
	private String md5;

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String getMd5()
	{
		return md5;
	}

	public void setMd5(String md5)
	{
		this.md5 = md5;
	}
}
