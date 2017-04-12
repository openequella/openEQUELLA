package com.tle.core.filesystem;

import com.tle.common.PathUtils;

@SuppressWarnings("nls")
public class AbstractTemplateFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String TEMPLATES_FOLDER = "Templates";

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), TEMPLATES_FOLDER);
	}
}
