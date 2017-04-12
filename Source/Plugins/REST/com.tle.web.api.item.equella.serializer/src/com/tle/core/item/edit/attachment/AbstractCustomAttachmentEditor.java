package com.tle.core.item.edit.attachment;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;

public abstract class AbstractCustomAttachmentEditor extends AbstractAttachmentEditor
{
	protected CustomAttachment customAttachment;

	@Override
	public boolean canEdit(Attachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			return ((CustomAttachment) attachment).getType().equals(getCustomType());
		}
		return false;
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		this.customAttachment = (CustomAttachment) attachment;
	}

	@Override
	public Attachment newAttachment()
	{
		CustomAttachment attachment = new CustomAttachment();
		attachment.setType(getCustomType());
		return attachment;
	}

	protected void editCustomData(String property, Object value)
	{
		if( hasBeenEdited(customAttachment.getData(property), value) )
		{
			customAttachment.setData(property, value);
		}
	}

	public abstract String getCustomType();
}
