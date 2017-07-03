package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;

/**
 * @author aholland
 */
@NonNullByDefault
public class AllExportFile extends AbstractRootFile
{
	private static final long serialVersionUID = 1L;

	private static final String EXPORT_FOLDER = "Export";

	public AllExportFile()
	{
		super(EXPORT_FOLDER);
	}
}
