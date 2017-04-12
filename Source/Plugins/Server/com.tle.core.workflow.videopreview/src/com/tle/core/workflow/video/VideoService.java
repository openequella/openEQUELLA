package com.tle.core.workflow.video;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.attachments.Attachment;

public interface VideoService
{
	boolean isVideo(Attachment attachment);

	boolean canConvertVideo(String filename);

	boolean videoPreviewExists(FileHandle handle, String filename);

	boolean makeGalleryVideoPreviews(FileHandle handle, String filename);
}
