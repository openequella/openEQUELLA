package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;

@NonNullByDefault
public class PublicFile extends AbstractPublicFile
{
	private static final long serialVersionUID = 1L;

	protected final String uuid;

	public PublicFile(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), FileHandleUtils.getHashedPath(uuid));
	}
}
