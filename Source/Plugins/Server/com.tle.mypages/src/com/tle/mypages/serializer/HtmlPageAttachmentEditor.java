package com.tle.mypages.serializer;

import com.tle.core.item.edit.attachment.AttachmentEditor;

public interface HtmlPageAttachmentEditor extends AttachmentEditor
{
	void editParentFolder(String parentFolder);

	void editHtml(String newUuid);
}
