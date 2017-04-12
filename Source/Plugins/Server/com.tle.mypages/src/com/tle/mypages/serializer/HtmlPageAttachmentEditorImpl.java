package com.tle.mypages.serializer;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractFileAttachmentEditor;

@Bind
public class HtmlPageAttachmentEditorImpl extends AbstractFileAttachmentEditor implements HtmlPageAttachmentEditor
{
	private HtmlAttachment htmlAttachment;

	@Override
	public void editParentFolder(String parentFolder)
	{
		if( hasBeenEdited(htmlAttachment.getParentFolder(), parentFolder) )
		{
			updateFileDetails(htmlAttachment.getFilename(), true);
			htmlAttachment.setParentFolder(parentFolder);
		}
		else if( changeTracker.isForceFileCheck() )
		{
			updateFileDetails(htmlAttachment.getFilename(), false);
		}
	}

	@Override
	public void editHtml(String newUuid)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		htmlAttachment = (HtmlAttachment) attachment;
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.HTML;
	}

	@Override
	public Attachment newAttachment()
	{
		return new HtmlAttachment();
	}

	@Override
	protected void setSize(long size)
	{
		htmlAttachment.setSize(size);
	}
}
