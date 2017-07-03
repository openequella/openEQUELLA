package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.filesystem.FileHandleUtils;

/**
 * @author aholland
 */
@NonNullByDefault
public class ExportFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public ExportFile(String name)
	{
		super(new AllExportFile(), name);

		FileHandleUtils.checkPath(name);
	}
}
