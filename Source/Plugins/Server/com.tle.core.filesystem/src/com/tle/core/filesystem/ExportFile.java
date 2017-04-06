package com.tle.core.filesystem;

/**
 * @author aholland
 */
public class ExportFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public ExportFile(String name)
	{
		super(new AllExportFile(), name);

		FileHandleUtils.checkPath(name);
	}

	@Override
	public String getMyPathComponent()
	{
		return name;
	}
}
