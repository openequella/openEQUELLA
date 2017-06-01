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

package com.tle.exceptions;

import com.tle.common.i18n.CurrentLocale;

public class TokenException extends BadCredentialsException
{
	public static final int STATUS_OK = 0;
	public static final int STATUS_TIME = 1;
	public static final int STATUS_SECRET = 2;
	public static final int STATUS_NOTOKEN = 3;
	public static final int STATUS_ERROR_IN_TOKEN = 4;
	public static final int STATUS_UNKNOWN = 5;
	public static final int STATUS_BAD_ID = 6;
	public static final int STATUS_USERNOTFOUND = 7;
	public static final int STATUS_NOPERMISSION = 8;

	int status;
	private Object[] values;

	public TokenException(int status, Object... values)
	{
		super("Error with Token!");
		this.status = status;
		this.values = values;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	@Override
	public String getLocalizedMessage()
	{
		return CurrentLocale.get(getMessage(), values);
	}

	@SuppressWarnings("nls")
	@Override
	public String getMessage()
	{
		String key;
		switch( status )
		{
			case STATUS_TIME:
				key = "com.tle.exceptions.time";
				break;
			case STATUS_NOTOKEN:
				key = "com.tle.exceptions.notoken";
				break;
			case STATUS_SECRET:
				key = "com.tle.exceptions.secret";
				break;
			case STATUS_ERROR_IN_TOKEN:
				key = "com.tle.exceptions.errorintoken";
				break;
			case STATUS_BAD_ID:
				key = "com.tle.exceptions.badtokenid";
				break;
			case STATUS_USERNOTFOUND:
				key = "com.tle.exceptions.badusername";
				break;
			case STATUS_NOPERMISSION:
				key = "com.tle.exceptions.nopermission";
				break;
			default:
				key = "com.tle.exceptions.unknown";
				break;
		}
		return key;
	}
}
