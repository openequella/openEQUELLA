package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public class ConversionFile extends AbstractFile
{
	private static final long serialVersionUID = 1L;

	public static final String EXPORT_PATH = "_EXPORT";

	private final FileHandle handle;

	public ConversionFile(FileHandle handle)
	{
		super(handle, EXPORT_PATH);
		this.handle = handle;
	}

	public FileHandle getHandle()
	{
		return handle;
	}
}
