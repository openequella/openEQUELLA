package com.tle.core.filesystem;

import com.tle.beans.filesystem.FileHandle;
import com.tle.common.PathUtils;

/**
 * @author aholland
 */
public abstract class AbstractFile implements FileHandle
{
	private static final long serialVersionUID = 1L;

	/**
	 * E.g. AllStaging, AllExport etc.
	 */
	protected final FileHandle parent;
	protected final String name;

	/**
	 * @param root Usually an AllXxxFile
	 * @param name
	 */
	protected AbstractFile(final FileHandle parent, final String name)
	{
		this.parent = parent;
		this.name = name;

		if( parent == null )
		{
			throw new IllegalArgumentException("A parent handle must be specified");
		}

		FileHandleUtils.checkPath(name);
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(parent.getAbsolutePath(), getMyPathComponent());
	}

	@Override
	public String getMyPathComponent()
	{
		return name;
	}

	@Override
	public final String toString()
	{
		return getAbsolutePath();
	}
}
