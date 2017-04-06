/*
 * Created on Dec 7, 2004
 */
package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class BannedFileException extends FileSystemException
{
	private static final long serialVersionUID = 1L;

	public BannedFileException(String filename)
	{
		super("'" + filename + "' has an extension that is barred from being uploaded");

		setShowStackTrace(false);
	}
}
