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

package com.tle.common.usermanagement.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.common.Constants;
import com.tle.common.URLUtils;
import com.tle.common.util.TokenGenerator;
import com.tle.exceptions.TokenException;

/**
 * @author jmaginnis
 */
public final class TokenSecurity
{
	private static final Log LOGGER = LogFactory.getLog(TokenSecurity.class);
	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(30);

	/**
	 * @param token returns null if completely invalid
	 * @return
	 * @throws IOException
	 */
	public static Token getInsecureToken(String token)
	{
		try
		{
			String[] parts = token.split(":", 5); //$NON-NLS-1$
			int i = 0;
			if( parts.length < 3 )
			{
				return null;
			}
			String username = URLUtils.basicUrlDecode(parts[i++]);
			String id = URLUtils.basicUrlDecode(parts[i++]);
			if( id.length() > 0 && !Character.isLetter(id.charAt(0)) )
			{
				i--;
				id = Constants.BLANK;
			}
			String time = URLUtils.basicUrlDecode(parts[i++]);
			if( i >= parts.length )
			{
				return null;
			}
			String base64 = parts[i++];
			String data = Constants.BLANK;
			if( i < parts.length )
			{
				data = parts[i++];
			}
			return new Token(username, id, Long.parseLong(time), base64, data);
		}
		catch( Exception e )
		{
			LOGGER.error("Error in token", e);
			throw new TokenException(TokenException.STATUS_ERROR_IN_TOKEN);
		}
	}

	public static boolean isSecureToken(Token data, String sharedSecret)
	{
		long time = System.currentTimeMillis();
		int status = verifySecureToken(data, sharedSecret, time);
		if( status == TokenException.STATUS_TIME )
		{
			String error = "Time difference for secure token too much: " + Math.abs(time - data.getTime());
			LOGGER.error(error);
			throw new TokenException(TokenException.STATUS_TIME);
		}
		if( status == TokenException.STATUS_SECRET )
		{
			throw new TokenException(TokenException.STATUS_SECRET);
		}
		return status == TokenException.STATUS_OK;
	}

	public static int verifySecureToken(Token data, String sharedSecret, long time)
	{
		if( data == null )
		{
			throw new TokenException(TokenException.STATUS_NOTOKEN);
		}
		long thetime = data.getTime();
		long diff = time - thetime;
		if( Math.abs(diff) > MAX_TIME )
		{
			return TokenException.STATUS_TIME;
		}

		String insecure = data.getInsecure();
		String match = insecure + data.getId() + thetime + sharedSecret;
		byte[] bytematch = TokenGenerator.getMd5Bytes(match);
		byte[] securebytes = Base64.decodeBase64(data.getBase64().getBytes());

		boolean ok = Arrays.equals(bytematch, securebytes);
		return ok ? TokenException.STATUS_OK : TokenException.STATUS_SECRET;
	}

	public static class Token
	{
		private String insecure;
		private String id;
		private long time;
		private String base64;
		private String data;

		public Token(String insecure, String id, long time, String base64, String data)
		{
			super();
			this.insecure = insecure;
			this.id = id;
			this.time = time;
			this.base64 = base64;
			this.data = data;
		}

		public String getInsecure()
		{
			return insecure;
		}

		public void setInsecure(String insecure)
		{
			this.insecure = insecure;
		}

		public String getBase64()
		{
			return base64;
		}

		public void setBase64(String base64)
		{
			this.base64 = base64;
		}

		public String getData()
		{
			return data;
		}

		public void setData(String data)
		{
			this.data = data;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public long getTime()
		{
			return time;
		}

		public void setTime(long time)
		{
			this.time = time;
		}
	}

	private TokenSecurity()
	{
		throw new Error();
	}
}
