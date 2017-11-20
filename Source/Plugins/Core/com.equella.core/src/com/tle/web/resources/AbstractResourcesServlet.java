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

package com.tle.web.resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.java.plugin.util.IoUtil;

import com.tle.common.PathUtils;
import com.tle.core.plugins.PluginService;
import com.tle.web.stream.ContentStream;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;
import com.tle.web.stream.URLContentStream;

@SuppressWarnings("nls")
public abstract class AbstractResourcesServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private PluginService pluginService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	protected void service(HttpServletRequest request, HttpServletResponse response, String resourcePath,
		String mimeType) throws IOException
	{
		final String filename = PathUtils.getFilenameFromFilepath(resourcePath);

		if( resourcePath.startsWith("/") )
		{
			resourcePath = resourcePath.substring(1);
		}

		final URL res = new URL(pluginService.getClassLoader(getPluginId(request)).getResource(getRootPath()),
			resourcePath);
		final File file = IoUtil.url2file(res);

		ContentStream stream;
		if( file != null )
		{
			stream = new FileContentStream(file, filename, mimeType);
		}
		else
		{
			stream = new URLContentStream(res, filename, mimeType);
		}
		contentStreamWriter.outputStream(request, response, stream);
	}

	public abstract String getRootPath();

	public abstract String getPluginId(HttpServletRequest request);
}