package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;

@SuppressWarnings("nls")
@NonNullByDefault
public class AllStagingFile extends AbstractRootFile
{
	private static final long serialVersionUID = 1L;

	private static final String STAGING_FOLDER = "Staging";

	public AllStagingFile()
	{
		super(STAGING_FOLDER);
	}
}
