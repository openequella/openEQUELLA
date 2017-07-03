package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class AllImportFile extends AbstractRootFile
{
	private static final long serialVersionUID = 1L;

	private static final String IMPORT_FOLDER = "Import";

	public AllImportFile()
	{
		super(IMPORT_FOLDER);
	}
}
