package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public abstract class AbstractPublicFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String PUBLIC_FOLDER = "Public";

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), PUBLIC_FOLDER);
	}
}
