package com.tle.web.api.item.equella.interfaces.beans;

public class ScormResourceAttachmentBean extends AbstractFileAttachmentBean
{

	@SuppressWarnings("nls")
	@Override
	public String getRawAttachmentType()
	{
		return "custom/scormres";
	}

}
