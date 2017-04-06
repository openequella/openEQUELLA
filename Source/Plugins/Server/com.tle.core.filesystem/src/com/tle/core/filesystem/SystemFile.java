package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class SystemFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String SYSTEM_FOLDER = "System";

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), SYSTEM_FOLDER);
	}
}
