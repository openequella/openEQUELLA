package com.tle.core.item.serializer;

import com.tle.core.item.edit.attachment.AttachmentEditor;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

public abstract class AbstractAttachmentSerializer implements AttachmentSerializer
{
	public void editStandard(AttachmentEditor editor, EquellaAttachmentBean attachment)
	{
		editor.editDescription(attachment.getDescription());
		editor.editPreview(attachment.isPreview());
		editor.editViewer(attachment.getViewer());
		editor.editRestricted(attachment.isRestricted());
		editor.editThumbnail(attachment.getThumbnail());
	}
}