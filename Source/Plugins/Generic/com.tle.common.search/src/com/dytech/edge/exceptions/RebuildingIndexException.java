package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class RebuildingIndexException extends SearchIndexException
{
	private static final long serialVersionUID = 1L;

	public RebuildingIndexException(Throwable th)
	{
		super("The search index could not be rebuilt", th);
	}

	@Override
	public boolean isShowStackTrace()
	{
		return true;
	}
}
