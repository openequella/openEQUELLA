package com.tle.core.item.edit.attachment;

public interface AttachmentEditor
{
	void editDescription(String description);

	void editViewer(String viewer);

	void editPreview(boolean preview);

	String getAttachmentUuid();

	void editRestricted(boolean restricted);

	void editThumbnail(String thumbnail);
}
