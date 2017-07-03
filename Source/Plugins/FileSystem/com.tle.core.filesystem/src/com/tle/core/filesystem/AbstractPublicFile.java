package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;

@NonNullByDefault
public abstract class AbstractPublicFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String PUBLIC_FOLDER = "Public";

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), PUBLIC_FOLDER);
	}
}
