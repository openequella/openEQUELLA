package com.dytech.edge.wizard;

/**
 * @author miken
 */
public class WizardException extends RuntimeException
{
	/**
	 * Creates new <code>WizardException</code> without detail message.
	 */
	public WizardException(Throwable t)
	{
		super(t);
	}

	public WizardException(String msg)
	{
		super(msg);
	}

	public WizardException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
