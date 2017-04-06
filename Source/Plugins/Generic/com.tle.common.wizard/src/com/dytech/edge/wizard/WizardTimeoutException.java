package com.dytech.edge.wizard;

/**
 * @author miken
 */
public class WizardTimeoutException extends WizardException
{
	/**
	 * Constructs an <code>WizardPageException</code> with the specified detail
	 * message.
	 * 
	 * @param msg the detail message.
	 */
	public WizardTimeoutException(String msg)
	{
		super(msg);
	}
}
