package com.tle.web.api.item.equella.interfaces.beans;

@SuppressWarnings("nls")
public class ScormAttachmentBean extends EquellaAttachmentBean
{
	private String packageFile;
	private String scormVersion;
	private long size;
	private String md5;

	public String getScormVersion()
	{
		return scormVersion;
	}

	public void setScormVersion(String scormVersion)
	{
		this.scormVersion = scormVersion;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/scorm";
	}

	public String getPackageFile()
	{
		return packageFile;
	}

	public void setPackageFile(String packageFile)
	{
		this.packageFile = packageFile;
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
