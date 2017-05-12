package com.tle.core.util.archive;

/**
 * @author aholland
 */
public abstract class ArchiveProgress
{
	private final long entryCount;

	public ArchiveProgress(final long entryCount)
	{
		this.entryCount = entryCount;
	}

	public long getEntryCount()
	{
		return entryCount;
	}

	public abstract void nextEntry(String entryPath);

	public void setCallbackMessageValue(String msg)
	{
		// Override where appropriate
	}

	public void incrementWarningCount()
	{
		// implement where appropriate
	}
}
