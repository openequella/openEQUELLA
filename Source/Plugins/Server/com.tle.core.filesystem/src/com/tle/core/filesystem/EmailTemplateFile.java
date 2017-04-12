package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class EmailTemplateFile extends AbstractTemplateFile
{
	private static final long serialVersionUID = 1L;

	private static final String EMAIL_FOLDER = "Email";

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), EMAIL_FOLDER);
	}
}
