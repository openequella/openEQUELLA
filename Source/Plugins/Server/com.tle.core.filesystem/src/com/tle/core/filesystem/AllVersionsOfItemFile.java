package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class AllVersionsOfItemFile extends AbstractAttachmentFile
{
	private static final long serialVersionUID = 1L;

	private final String uuid;

	public AllVersionsOfItemFile(String uuid)
	{
		this.uuid = uuid;

		FileHandleUtils.checkPath(uuid);
	}

	public String getUuid()
	{
		return uuid;
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), FileHandleUtils.getHashedPath(uuid));
	}
}
