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

package com.tle.web.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.io.Closeables;

public class URLTemplateSource
{
	private final URL url;
	private URLConnection conn;
	private boolean calledLastModified;
	private boolean calledGetInputStream;

	URLTemplateSource(URL url) throws IOException
	{
		this.url = url;
		conn = url.openConnection();
	}

	@Override
	public boolean equals(Object o)
	{
		if( this == o )
		{
			return true;
		}

		if( !(o instanceof URLTemplateSource) )
		{
			return false;
		}

		return url.toString().equals(((URLTemplateSource) o).url.toString());
	}

	@Override
	public int hashCode()
	{
		return url.toString().hashCode();
	}

	@Override
	public String toString()
	{
		return url.toString();
	}

	long lastModified()
	{
		calledLastModified = true;
		return conn.getLastModified();
	}

	InputStream getInputStream() throws IOException
	{
		calledGetInputStream = true;
		return conn.getInputStream();
	}

	void close() throws IOException
	{
		if( !calledGetInputStream && calledLastModified )
		{
			Closeables.close(getInputStream(), true);
		}
	}
}
