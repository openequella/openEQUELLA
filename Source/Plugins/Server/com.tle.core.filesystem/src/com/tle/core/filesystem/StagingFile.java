/*
 * Created on Oct 25, 2005
 */
package com.tle.core.filesystem;

/**
 * @author Nicholas Read
 */
public class StagingFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public StagingFile(String uuid)
	{
		super(new AllStagingFile(), uuid);

		FileHandleUtils.checkPath(name);
	}

	public String getUuid()
	{
		return name;
	}

	@Override
	public String getMyPathComponent()
	{
		return FileHandleUtils.getHashedPath(name);
	}
}
