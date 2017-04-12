package com.tle.core.metadata;

import java.io.File;
import java.util.Map;

import com.google.common.cache.LoadingCache;
import com.tle.beans.item.attachments.Attachment;

public interface MetadataHandler
{
	void getMetadata(LoadingCache<String, Map<String, String>> metadata, Attachment a);

	void getMetadata(LoadingCache<String, Map<String, String>> metadata, File f);
}
