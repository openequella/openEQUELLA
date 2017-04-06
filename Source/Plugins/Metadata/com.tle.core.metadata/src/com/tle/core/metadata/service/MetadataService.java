package com.tle.core.metadata.service;

import java.io.File;
import java.util.Map;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.attachments.Attachment;

public interface MetadataService
{
	Map<String, Map<String, String>> getMetadata(File f);

	Map<String, Map<String, String>> getMetadata(Attachment a, FileHandle handle);
}
