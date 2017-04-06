package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class PublicFile extends AbstractPublicFile
{
	private static final long serialVersionUID = 1L;

	protected final String uuid;

	public PublicFile(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), FileHandleUtils.getHashedPath(uuid));
	}
}
