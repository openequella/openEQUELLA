package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class AbstractAttachmentFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String ATTACHMENTS_FOLDER = "Attachments";

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), ATTACHMENTS_FOLDER);
	}
}
