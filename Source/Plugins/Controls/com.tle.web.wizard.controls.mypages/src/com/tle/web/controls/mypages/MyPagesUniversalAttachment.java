package com.tle.web.controls.mypages;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.web.controls.universal.UniversalAttachment;

public class MyPagesUniversalAttachment implements UniversalAttachment
{
	private HtmlAttachment htmlAttachment;

	public MyPagesUniversalAttachment(HtmlAttachment htmlAttachment)
	{
		this.htmlAttachment = htmlAttachment;
	}

	@Override
	public Attachment getAttachment()
	{
		return htmlAttachment;
	}

}
