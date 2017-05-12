package com.tle.core.util.archive;

public class ArchiveEntry
{
	private final String name;
	private final boolean directory;
	private final long size;

	public ArchiveEntry(String name, boolean directory, long size)
	{
		this.name = name;
		this.directory = directory;
		this.size = size;
	}

	public String getName()
	{
		return name;
	}

	public boolean isDirectory()
	{
		return directory;
	}

	public long getSize()
	{
		return size;
	}
}