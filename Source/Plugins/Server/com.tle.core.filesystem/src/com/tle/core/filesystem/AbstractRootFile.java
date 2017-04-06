package com.tle.core.filesystem;

import com.tle.beans.filesystem.FileHandle;

/**
 * @author aholland
 */
public abstract class AbstractRootFile implements FileHandle
{
	private static final long serialVersionUID = 1L;

	private final String path;

	protected AbstractRootFile(final String path)
	{
		this.path = path;
	}

	@Override
	public String getAbsolutePath()
	{
		return getMyPathComponent();
	}

	@Override
	public String getMyPathComponent()
	{
		return path;
	}
}
