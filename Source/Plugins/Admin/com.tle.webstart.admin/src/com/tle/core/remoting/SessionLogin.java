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

package com.tle.core.remoting;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.tle.common.URLUtils;
import com.tle.exceptions.BadCredentialsException;

public class SessionLogin
{

	public static void postLogin(URL endpointUrl, Map<String, String> params)
		throws IOException, BadCredentialsException
	{
		HttpURLConnection conn = (HttpURLConnection) new URL(endpointUrl, "session").openConnection();
		String postData = URLUtils.getParameterString(params);
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
		conn.setRequestProperty("Content-Length", Integer.toString(postBytes.length));
		conn.setUseCaches(false);
		try( OutputStream wr = conn.getOutputStream() )
		{
			wr.write(postBytes);
		}

		int code = conn.getResponseCode();
		if( code == 403 || code == 401 )
		{
			throw new BadCredentialsException("Bad credentials");
		}
		else if (code != 200)
		{
			throw new RuntimeException("Error launching console: " + code);
		}
	}
}
