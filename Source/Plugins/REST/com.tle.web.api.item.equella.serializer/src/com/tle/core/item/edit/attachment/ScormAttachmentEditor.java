package com.tle.core.item.edit.attachment;

public interface ScormAttachmentEditor extends AttachmentEditor
{
	void editPackageFile(String filename);

	// Probably should be worked out from file
	void editScormVersion(String version);
}
