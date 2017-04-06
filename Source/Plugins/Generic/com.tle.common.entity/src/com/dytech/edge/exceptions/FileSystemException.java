/*
 * Created on Dec 7, 2004
 */
package com.dytech.edge.exceptions;

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
