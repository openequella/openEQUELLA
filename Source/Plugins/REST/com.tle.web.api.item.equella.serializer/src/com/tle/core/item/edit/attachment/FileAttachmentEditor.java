package com.tle.core.item.edit.attachment;

public interface FileAttachmentEditor extends AttachmentEditor
{
	void editFilename(String filename);

	void editConversion(boolean convertible);

	void editZipParent(String parentZip);
}
