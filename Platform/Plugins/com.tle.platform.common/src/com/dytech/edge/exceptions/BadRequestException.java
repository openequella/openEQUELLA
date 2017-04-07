package com.dytech.edge.exceptions;

/**
 * @author Aaron
 */
public class BadRequestException extends WebException
{
	private static final long serialVersionUID = 1L;

	private final String parameter;

	@SuppressWarnings("nls")
	public BadRequestException(String parameter)
	{
		super(400, "bad_request", parameter);
		this.parameter = parameter;
	}

	public String getParameter()
	{
		return parameter;
	}
}
