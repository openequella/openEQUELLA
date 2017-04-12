package com.tle.web.controls.resource;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.web.controls.universal.UniversalAttachment;
import com.tle.web.selection.SelectedResourceDetails;

public class ResourceUniversalAttachment implements UniversalAttachment
{
	private final SelectedResourceDetails selection;
	private final CustomAttachment attachment;

	public ResourceUniversalAttachment(SelectedResourceDetails selection, CustomAttachment attachment)
	{
		this.attachment = attachment;
		this.selection = selection;
	}

	@Override
	public Attachment getAttachment()
	{
		return attachment;
	}

	public SelectedResourceDetails getSelection()
	{
		return selection;
	}

}
