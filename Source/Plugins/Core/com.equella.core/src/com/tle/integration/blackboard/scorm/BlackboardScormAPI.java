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

package com.tle.integration.blackboard.scorm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.tle.common.URLUtils;
import com.tle.web.scorm.ScormAPI;

public class BlackboardScormAPI implements ScormAPI, Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(BlackboardScormAPI.class);

	private String cookies;
	private URL servletURL;

	public BlackboardScormAPI(URL servletURL, String cookies)
	{
		this();
		LOGGER.debug("new BlackboardScormAPI");
		LOGGER.debug("servletURL: " + servletURL.toString());
		LOGGER.debug("cookies: " + cookies);
		this.servletURL = servletURL;
		this.cookies = cookies;
	}

	/**
	 * Only for serializable calling
	 */
	public BlackboardScormAPI()
	{
		//
	}

	private String call(String name, String[] params)
	{
		HttpURLConnection con = null;
		try
		{
			con = (HttpURLConnection) servletURL.openConnection();
			con.setRequestProperty("Cookie", cookies); //$NON-NLS-1$
			con.setDoOutput(true);
			con.setDoInput(true);

			StringBuilder paramBuilder = new StringBuilder();
			paramBuilder.append("method="); //$NON-NLS-1$
			paramBuilder.append(name);
			for( int i = 0; i < params.length; i++ )
			{
				paramBuilder.append("&param"); //$NON-NLS-1$
				paramBuilder.append(Integer.toString(i));
				paramBuilder.append("="); //$NON-NLS-1$
				paramBuilder.append(URLUtils.urlEncode(Strings.nullToEmpty(params[i])));
			}
			LOGGER.debug("Contacting: " + servletURL.toString() + '&' + paramBuilder.toString());

			try( OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(con.getOutputStream())) )
			{
				writer.write(paramBuilder.toString());
			}

			try( BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream())) )
			{
				String line = reader.readLine();
				LOGGER.debug("Response: " + line);
				// This can happen if the returned string is "" (ie we never
				// want to
				// return null)
				if( line == null )
				{
					line = ""; //$NON-NLS-1$
				}
				return line;
			}
		}
		catch( IOException e )
		{
			LOGGER.error("Error contacting Blackboard SCORM servlet", e);
			return "false";
		}
		finally
		{
			if( con != null )
			{
				con.disconnect();
			}
		}
	}

	@Override
	public String initialize(String param)
	{
		return call("Initialize", new String[]{param}); //$NON-NLS-1$
	}

	@Override
	public String terminate(String param)
	{
		return call("Terminate", new String[]{param}); //$NON-NLS-1$
	}

	@Override
	public String getValue(String name)
	{
		return call("GetValue", new String[]{name}); //$NON-NLS-1$
	}

	@Override
	public String setValue(String name, String value)
	{
		return call("SetValue", new String[]{name, value}); //$NON-NLS-1$
	}

	@Override
	public String commit(String param)
	{
		return call("Commit", new String[]{param}); //$NON-NLS-1$
	}

	@Override
	public String getLastError()
	{
		return call("GetLastError", new String[]{}); //$NON-NLS-1$
	}

	@Override
	public String getErrorString(String errCode)
	{
		return call("GetErrorString", new String[]{errCode}); //$NON-NLS-1$
	}

	@Override
	public String getDiagnostic(String error)
	{
		return call("GetDiagnostic", new String[]{error}); //$NON-NLS-1$
	}

	@Override
	public void setCurrentIdentifier(String ident)
	{
		call("setCurrentIdentifier", new String[]{ident}); //$NON-NLS-1$
	}
}
