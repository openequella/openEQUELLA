/*
 * Created on Oct 25, 2005
 */
package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.filesystem.FileHandleUtils;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public class StagingFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;
	private String cachedPathComponent;

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
		if( cachedPathComponent == null )
		{
			cachedPathComponent = FileHandleUtils.getHashedPath(name);
		}
		return cachedPathComponent;
	}
}
