/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.common.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

import com.dytech.edge.common.Constants;

public final class TokenGenerator
{
	public static String createSecureToken(String token, String id, String sharedSecret, String data)
		throws IOException
	{
		return createSecureToken(token, id, sharedSecret, data, System.currentTimeMillis());
	}

	public static String createSecureToken(String token, String id, String sharedSecret, String data, long curTime)
		throws IOException
	{
		if( id == null )
		{
			id = Constants.BLANK;
		}

		String time = Long.toString(curTime);
		String toMd5 = token + id + time + sharedSecret;

		StringBuilder b = new StringBuilder();
		b.append(URLEncoder.encode(token, Constants.UTF8));
		b.append(':');
		b.append(URLEncoder.encode(id, Constants.UTF8));
		b.append(':');
		b.append(time);
		b.append(':');
		b.append(new String(Base64.encodeBase64(getMd5Bytes(toMd5))));
		if( data != null && data.length() > 0 )
		{
			b.append(':');
			b.append(data);
		}
		return b.toString();
	}

	public static byte[] getMd5Bytes(String str)
	{
		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			digest.update(str.getBytes(Constants.UTF8));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		return digest.digest();
	}

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private TokenGenerator()
	{
		throw new Error();
	}
}
