package com.tle.web.api.item.equella.interfaces.beans;

public class HtmlPageAttachmentBean extends AbstractFileAttachmentBean
{
	private static final String TYPE = "html";

	private String parentFolder;

	public String getParentFolder()
	{
		return parentFolder;
	}

	public void setParentFolder(String parentFolder)
	{
		this.parentFolder = parentFolder;
	}

	@Override
	public String getRawAttachmentType()
	{
		return TYPE;
	}
}
