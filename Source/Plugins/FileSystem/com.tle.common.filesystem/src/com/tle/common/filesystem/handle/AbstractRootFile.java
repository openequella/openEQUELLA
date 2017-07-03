package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author aholland
 */
@NonNullByDefault
public abstract class AbstractRootFile implements FileHandle
{
	private static final long serialVersionUID = 1L;

	private final String path;
	@Nullable
	private String filestoreId;
	@Nullable
	private String cachedAbsolutePath;

	protected AbstractRootFile(final String path)
	{
		this.path = path;
	}

	@Override
	public final String getAbsolutePath()
	{
		if( cachedAbsolutePath == null )
		{
			cachedAbsolutePath = createAbsolutePath();
		}
		return cachedAbsolutePath;
	}

	protected String createAbsolutePath()
	{
		return getMyPathComponent();
	}

	@Override
	public String getMyPathComponent()
	{
		return path;
	}

	@Nullable
	@Override
	public String getFilestoreId()
	{
		return filestoreId;
	}

	public void setFilestoreId(@Nullable String filestoreId)
	{
		this.filestoreId = filestoreId;
	}
}
