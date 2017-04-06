/**
 * 
 */
package com.tle.core.reporting.birttypes;

public class BirtTypeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public BirtTypeException(String e)
	{
		super("Error in Birt Type because " + e); //$NON-NLS-1$
	}
}