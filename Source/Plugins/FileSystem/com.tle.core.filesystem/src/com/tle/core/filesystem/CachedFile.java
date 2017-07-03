package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;

@NonNullByDefault
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
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), hashedPath);
	}
}