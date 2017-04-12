package com.tle.web.api.item.equella.interfaces.beans;

public class PackageResourceAttachmentBean extends AbstractFileAttachmentBean
{
	private static final String TYPE = "imsres";

	@Override
	public String getRawAttachmentType()
	{
		return TYPE;
	}
}
