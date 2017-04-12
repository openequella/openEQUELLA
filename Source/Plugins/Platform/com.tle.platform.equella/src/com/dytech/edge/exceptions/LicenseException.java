/*
 * Created on Oct 26, 2004 For "The Learning Edge"
 */
package com.dytech.edge.exceptions;

/**
 * @author jmaginnis
 */
public class LicenseException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public LicenseException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public LicenseException(String msg)
	{
		super(msg);
	}
}
