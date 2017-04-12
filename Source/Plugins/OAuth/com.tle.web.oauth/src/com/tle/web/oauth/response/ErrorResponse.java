package com.tle.web.oauth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse
{
	private String error;
	private String errorDescription;
	private String errorUri;
	private String state;

	@JsonProperty("error")
	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	@JsonProperty("error_description")
	public String getErrorDescription()
	{
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription)
	{
		this.errorDescription = errorDescription;
	}

	public String getErrorUri()
	{
		return errorUri;
	}

	@JsonProperty("error_uri")
	public void setErrorUri(String errorUri)
	{
		this.errorUri = errorUri;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}
}