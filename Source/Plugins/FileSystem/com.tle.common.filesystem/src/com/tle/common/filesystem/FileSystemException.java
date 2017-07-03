/*
 * Created on Dec 7, 2004
 */
package com.tle.common.filesystem;

import com.dytech.edge.exceptions.RuntimeApplicationException;

/**
 * @author Nicholas Read
 */
public class FileSystemException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public FileSystemException(String message)
	{
		super(message);
	}

	public FileSystemException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
