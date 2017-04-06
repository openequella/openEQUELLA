package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class CachedFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;
	private final String hashedPath;

	public CachedFile(String uuid)
	{
		super();
		hashedPath = PathUtils.filePath("Cache", FileHandleUtils.getHashedPath(uuid));
		FileHandleUtils.checkPath(hashedPath);
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), hashedPath);
	}
}