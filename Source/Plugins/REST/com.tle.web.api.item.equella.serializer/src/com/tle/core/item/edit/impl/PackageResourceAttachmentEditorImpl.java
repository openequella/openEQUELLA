package com.tle.core.item.edit.impl;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.PackageResourceAttachmentEditor;

@Bind
public class PackageResourceAttachmentEditorImpl extends AbstractAttachmentEditor
	implements
		PackageResourceAttachmentEditor
{

	private IMSResourceAttachment resAttachment;

	@Override
	public void editFilename(String filename)
	{
		if( hasBeenEdited(resAttachment.getUrl(), filename) )
		{
			resAttachment.setUrl(filename);
		}
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		this.resAttachment = (IMSResourceAttachment) attachment;
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.IMSRES;
	}

	@Override
	public Attachment newAttachment()
	{
		return new IMSResourceAttachment();
	}

}
