package com.tle.web.oauth;

import com.dytech.edge.exceptions.QuietlyLoggable;
import com.dytech.edge.exceptions.WebException;

/**
 * @author Aaron
 */
public class OAuthException extends WebException implements QuietlyLoggable
{
	private static final long serialVersionUID = 1L;

	private boolean badClientOrRedirectUri;

	public OAuthException(int code, String error, String message)
	{
		super(code, error, message);
	}

	public OAuthException(int code, String error, String message, boolean badClientOrRedirectUri)
	{
		super(code, error, message);
		this.badClientOrRedirectUri = badClientOrRedirectUri;
	}

	public boolean isBadClientOrRedirectUri()
	{
		return badClientOrRedirectUri;
	}

	public void setBadClientOrRedirectUri(boolean badClientOrRedirectUri)
	{
		this.badClientOrRedirectUri = badClientOrRedirectUri;
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}

	@Override
	public boolean isSilent()
	{
		return false;
	}

	@Override
	public boolean isWarnOnly()
	{
		return true;
	}
}
