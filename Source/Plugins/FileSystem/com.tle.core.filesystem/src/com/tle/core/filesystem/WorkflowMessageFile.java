package com.tle.core.filesystem;

import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;

public class WorkflowMessageFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	protected final String uuid;

	public WorkflowMessageFile(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public String createAbsolutePath()
	{
		String filePath = PathUtils.filePath(super.createAbsolutePath(), "WorkflowMessage");
		return PathUtils.filePath(filePath, FileHandleUtils.getHashedPath(uuid));
	}
}