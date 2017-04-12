package com.tle.core.item.edit.impl;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.UrlAttachmentEditor;

@Bind
public class UrlAttachmentEditorImpl extends AbstractAttachmentEditor implements UrlAttachmentEditor
{
	private LinkAttachment urlAttachment;

	@Override
	public void editUrl(String url)
	{
		if( hasBeenEdited(urlAttachment.getUrl(), url) )
		{
			urlAttachment.setUrl(url);
		}
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		this.urlAttachment = (LinkAttachment) attachment;
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.LINK;
	}

	@Override
	public Attachment newAttachment()
	{
		return new LinkAttachment();
	}

}
