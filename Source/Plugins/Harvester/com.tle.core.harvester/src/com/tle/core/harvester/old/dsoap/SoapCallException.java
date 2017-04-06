package com.tle.core.harvester.old.dsoap;

/**
 * @author gfrancis
 */
public class SoapCallException extends Exception
{
	private int error;
	private String html;

	/**
	 * Creates a new instance of <code>SoapCallException</code> without detail
	 * message.
	 */
	public SoapCallException()
	{
		super();
	}

	/**
	 * Constructs an instance of <code>SoapCallException</code> with the
	 * specified detail message.
	 * 
	 * @param msg the detail message.
	 */
	public SoapCallException(String msg)
	{
		super(msg);
	}

	public SoapCallException(String msg, int code)
	{
		super(msg);
		error = code;
	}

	public SoapCallException(String msg, int code, String html)
	{
		super(msg);
		error = code;
		this.html = html;
	}

	public SoapCallException(Exception ex)
	{
		super(ex);
	}

	public SoapCallException(Exception ex, int code)
	{
		super(ex);
		error = code;
	}

	public int getErrorCode()
	{
		return error;
	}

	public String getHTML()
	{
		return html;
	}
}