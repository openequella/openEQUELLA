package com.tle.mypages.web.model;

import java.util.List;

import com.tle.beans.item.attachments.Attachment;

/**
 * @author aholland
 */
public class MyPagesAttachmentsModel
{
	private List<Attachment> attachments;

	public List<Attachment> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments)
	{
		this.attachments = attachments;
	}
}
