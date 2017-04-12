package com.tle.core.item.edit.attachment;

public interface ZipAttachmentEditor extends AttachmentEditor
{
	void editFolder(String folderPath);

	void editMapped(boolean mapped);
}
