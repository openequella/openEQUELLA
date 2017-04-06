package com.tle.core.filesystem;

public class SubTemporaryFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public SubTemporaryFile(final TemporaryFileHandle parent, final String extraPath)
	{
		super(parent, FileSystemHelper.encode(extraPath));
	}
}
