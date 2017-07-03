package com.tle.core.quickupload.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Pair;

public interface QuickUploadService
{
	ItemDefinition getOneClickItemDef();

	Pair<ItemId, Attachment> createOrSelectExisting(InputStream inputStream, String filename) throws IOException;
	Pair<ItemId, Attachment> createOrSelectExisting(InputStream inputStream, String filename,
		Map<String, List<String>> params)
		throws IOException;
}