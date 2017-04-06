package com.tle.core.item.edit.impl;

import static com.tle.core.util.ims.IMSUtilities.KEY_EXPAND_IMS_PACKAGE;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractFileAttachmentEditor;
import com.tle.core.item.edit.attachment.PackageAttachmentEditor;

@Bind
public class PackageAttachmentEditorImpl extends AbstractFileAttachmentEditor implements PackageAttachmentEditor
{

	private ImsAttachment pkgAttachment;

	@SuppressWarnings("nls")
	@Override
	public void editPackageFile(String filename)
	{
		if( hasBeenEdited(pkgAttachment.getUrl(), filename) )
		{
			updateFileDetails("_IMS/" + filename, true);
			pkgAttachment.setUrl(filename);
		}
		else if( changeTracker.isForceFileCheck() )
		{
			updateFileDetails("_IMS/" + filename, false);
		}
	}

	@Override
	protected void setSize(long size)
	{
		pkgAttachment.setSize(size);
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		pkgAttachment = (ImsAttachment) attachment;
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.IMS;
	}

	@Override
	public Attachment newAttachment()
	{
		return new ImsAttachment();
	}

	@Override
	public void setExpand(boolean expand)
	{
		if( hasBeenEdited(pkgAttachment.getData(KEY_EXPAND_IMS_PACKAGE), expand) )
		{
			pkgAttachment.setData(KEY_EXPAND_IMS_PACKAGE, expand);
		}
	}

}
