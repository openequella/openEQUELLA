package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;

@NonNullByDefault
public class EmailTemplateFile extends AbstractTemplateFile
{
	private static final long serialVersionUID = 1L;

	private static final String EMAIL_FOLDER = "Email";

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), EMAIL_FOLDER);
	}
}
