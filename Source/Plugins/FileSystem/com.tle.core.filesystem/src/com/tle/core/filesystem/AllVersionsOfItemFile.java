package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;

@NonNullByDefault
public class AllVersionsOfItemFile extends AbstractAttachmentFile
{
	private static final long serialVersionUID = 1L;

	private final String itemUuid;

	public AllVersionsOfItemFile(String itemUuid, @Nullable String collectionUuid)
	{
		super(collectionUuid);
		this.itemUuid = itemUuid;

		FileHandleUtils.checkPath(itemUuid);
	}

	public String getUuid()
	{
		return itemUuid;
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), FileHandleUtils.getHashedPath(itemUuid));
	}
}
