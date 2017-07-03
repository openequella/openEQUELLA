package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.filesystem.FileHandleUtils;

/**
 * @author aholland
 */
@NonNullByDefault
public class ImportFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public ImportFile(String name)
	{
		super(new AllImportFile(), name);

		FileHandleUtils.checkPath(name);
	}
}
