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

package com.tle.web.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLContentStream extends AbstractContentStream
{
	private URLConnection urlConnection;

	public URLContentStream(URL url, String filepath, String mimeType) throws IOException
	{
		super(filepath, mimeType);
		urlConnection = url.openConnection();
	}

	@Override
	public boolean exists()
	{
		try
		{
			urlConnection.connect();
			return true;
		}
		catch( IOException e )
		{
			return false;
		}
	}

	@Override
	public long getContentLength()
	{
		return urlConnection.getContentLength();
	}

	@Override
	public long getLastModified()
	{
		return urlConnection.getLastModified();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return urlConnection.getInputStream();
	}

}
