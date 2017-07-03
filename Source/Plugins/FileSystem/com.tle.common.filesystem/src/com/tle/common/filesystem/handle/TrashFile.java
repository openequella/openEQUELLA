package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;

@NonNullByDefault
public class TrashFile extends AbstractRootFile
{
	private static final long serialVersionUID = 1L;

	private static final String TRASH_FOLDER = "Trash";

	public TrashFile(TemporaryFileHandle staging)
	{
		super(PathUtils.filePath(TRASH_FOLDER, staging.getMyPathComponent()));
	}
}
