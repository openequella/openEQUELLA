package com.tle.web.viewurl;

public class ViewAuditEntry
{
	private String contentType;
	private String path;
	private boolean summary;

	public ViewAuditEntry(String contentType, String path)
	{
		this.contentType = contentType;
		this.path = path;
		this.summary = false;
	}

	public ViewAuditEntry(boolean summary)
	{
		this.summary = summary;
	}

	public String getPath()
	{
		return path;
	}

	public String getContentType()
	{
		return contentType;
	}

	public boolean isSummary()
	{
		return summary;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public void setSummary(boolean summary)
	{
		this.summary = summary;
	}
}
