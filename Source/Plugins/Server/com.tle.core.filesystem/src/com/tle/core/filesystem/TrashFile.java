package com.tle.core.filesystem;

import com.tle.common.PathUtils;

public class TrashFile extends AbstractRootFile
{
	private static final long serialVersionUID = 1L;

	private static final String TRASH_FOLDER = "Trash";

	public TrashFile(TemporaryFileHandle staging)
	{
		super(PathUtils.filePath(TRASH_FOLDER, staging.getMyPathComponent()));
	}
}
