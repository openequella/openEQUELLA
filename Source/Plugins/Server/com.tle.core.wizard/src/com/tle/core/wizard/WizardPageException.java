package com.tle.core.wizard;

/**
 * @author miken
 */
public class WizardPageException extends Exception
{
	/**
	 * Creates new <code>WizardPageException</code> without detail message.
	 */
	public WizardPageException()
	{
		super();
	}

	/**
	 * Constructs an <code>WizardPageException</code> with the specified detail
	 * message.
	 * 
	 * @param msg the detail message.
	 */
	public WizardPageException(String msg)
	{
		super(msg);
	}

	public WizardPageException(Throwable t)
	{
		super(t);
	}
}
