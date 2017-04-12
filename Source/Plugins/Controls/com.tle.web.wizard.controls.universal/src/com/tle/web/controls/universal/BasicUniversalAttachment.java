package com.tle.web.controls.universal;

import com.tle.beans.item.attachments.Attachment;

public class BasicUniversalAttachment implements UniversalAttachment
{
	private final Attachment attachment;

	public BasicUniversalAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}

	@Override
	public Attachment getAttachment()
	{
		return attachment;
	}

}
