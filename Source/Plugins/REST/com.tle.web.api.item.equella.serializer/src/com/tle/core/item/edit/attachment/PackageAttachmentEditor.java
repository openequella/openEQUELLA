package com.tle.core.item.edit.attachment;

public interface PackageAttachmentEditor extends AttachmentEditor
{
	void editPackageFile(String filename);

	void setExpand(boolean expand);
}
