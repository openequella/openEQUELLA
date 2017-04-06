package com.tle.core.item.edit.impl;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.ZipAttachmentEditor;

@Bind
public class ZipAttachmentEditorImpl extends AbstractAttachmentEditor implements ZipAttachmentEditor
{
	private ZipAttachment zipAttachment;

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		zipAttachment = (ZipAttachment) attachment;
	}

	@Override
	public void editFolder(String folderPath)
	{
		if( hasBeenEdited(zipAttachment.getUrl(), folderPath) )
		{
			zipAttachment.setUrl(folderPath);
		}
	}

	@Override
	public void editMapped(boolean mapped)
	{
		if( hasBeenEdited(zipAttachment.isMapped(), mapped) )
		{
			zipAttachment.setMapped(mapped);
		}
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.ZIP;
	}

	@Override
	public Attachment newAttachment()
	{
		return new ZipAttachment();
	}
}