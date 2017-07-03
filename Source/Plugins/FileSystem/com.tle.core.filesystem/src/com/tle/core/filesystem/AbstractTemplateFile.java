package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;

@SuppressWarnings("nls")
@NonNullByDefault
public class AbstractTemplateFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String TEMPLATES_FOLDER = "Templates";

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), TEMPLATES_FOLDER);
	}
}
