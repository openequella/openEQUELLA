package com.tle.web.api.item.equella.interfaces.beans;

@SuppressWarnings("nls")
public class PackageAttachmentBean extends EquellaAttachmentBean
{
	private long size;
	private String packageFile;
	private boolean expand;

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String getPackageFile()
	{
		return packageFile;
	}

	public void setPackageFile(String packageFile)
	{
		this.packageFile = packageFile;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "ims";
	}

	public boolean isExpand()
	{
		return expand;
	}

	public void setExpand(boolean expand)
	{
		this.expand = expand;
	}
}
