package com.tle.core.workflow.thumbnail;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.core.filesystem.FileHandleUtils;
import com.tle.core.filesystem.InstitutionFile;

@SuppressWarnings("nls")
@NonNullByDefault
public class ThumbnailQueueFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;
	private static final String THUMB_QUEUE_FOLDER = "ThumbQueue";

	private final String requestUuid;

	public ThumbnailQueueFile(String requestUuid)
	{
		this.requestUuid = requestUuid;
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), THUMB_QUEUE_FOLDER,
			FileHandleUtils.getHashedPath(requestUuid));
	}
}