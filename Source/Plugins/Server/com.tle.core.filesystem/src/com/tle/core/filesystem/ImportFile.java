package com.tle.core.filesystem;

/**
 * @author aholland
 */
public class ImportFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public ImportFile(String name)
	{
		super(new AllImportFile(), name);

		FileHandleUtils.checkPath(name);
	}
}
