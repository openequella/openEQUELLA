package com.dytech.edge.exceptions;

/**
 * @author Aaron
 */
public class WebException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final int code;
	private final String error;

	/**
	 * @param code
	 * @param error A short string uniquely identifying the error
	 * @param message
	 */
	public WebException(int code, String error, String message)
	{
		super(message);
		this.error = error;
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

	/**
	 * @return A short string uniquely identifying the error
	 */
	public String getError()
	{
		return error;
	}
}