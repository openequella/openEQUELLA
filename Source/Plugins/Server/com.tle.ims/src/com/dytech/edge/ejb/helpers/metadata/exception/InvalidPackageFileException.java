package com.dytech.edge.ejb.helpers.metadata.exception;

import java.util.List;

/**
 * @author aholland
 */
public class InvalidPackageFileException extends Exception
{
	private final List<String> formats;

	public InvalidPackageFileException(String message, List<String> formats)
	{
		super(message);
		this.formats = formats;
	}

	public List<String> getFormats()
	{
		return formats;
	}
}
