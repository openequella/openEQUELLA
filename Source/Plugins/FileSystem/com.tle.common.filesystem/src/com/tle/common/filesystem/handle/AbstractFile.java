package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;

/**
 * @author aholland
 */
@NonNullByDefault
public abstract class AbstractFile implements FileHandle
{
	private static final long serialVersionUID = 1L;

	/**
	 * E.g. AllStaging, AllExport etc.
	 */
	protected final FileHandle parent;
	protected final String name;
	@Nullable
	private String cachedAbsolutePath;

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
	public final String getAbsolutePath()
	{
		if( cachedAbsolutePath == null )
		{
			cachedAbsolutePath = PathUtils.filePath(parent.getAbsolutePath(), getMyPathComponent());
		}
		return cachedAbsolutePath;
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

	@Nullable
	@Override
	public String getFilestoreId()
	{
		return null;
	}
}
