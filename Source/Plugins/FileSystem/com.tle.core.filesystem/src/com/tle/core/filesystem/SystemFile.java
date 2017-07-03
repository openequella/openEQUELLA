package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;

@NonNullByDefault
public class SystemFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String SYSTEM_FOLDER = "System";

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), SYSTEM_FOLDER);
	}
}
