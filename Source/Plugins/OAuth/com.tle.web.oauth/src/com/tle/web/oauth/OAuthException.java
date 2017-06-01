/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
