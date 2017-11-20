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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.tle.common.PathUtils;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ResourcesServlet extends AbstractResourcesServlet
{
	private static Map<String, String> CONTENT_TYPES;

	static
	{
		Builder<String, String> builder = ImmutableMap.builder();
		builder.put("css", "text/css").put("js", "application/javascript").put("png", "image/png")
			.put("jpeg", "image/jpeg").put("jpg", "image/jpeg").put("gif", "image/gif")
			.put("woff", "application/x-font-woff");
		CONTENT_TYPES = builder.build();
	}

	private final String maxAge;
	private final long expires;

	@Inject
	private MimeTypeService mimeService;

	public ResourcesServlet()
	{
		long twentyFiveYearsInMillis = TimeUnit.DAYS.toMillis(365) * 25;

		maxAge = "max-age=" + TimeUnit.MILLISECONDS.toSeconds(twentyFiveYearsInMillis) + ", public";
		expires = System.currentTimeMillis() + twentyFiveYearsInMillis;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		final String pathInfo = request.getPathInfo();

		// Eg, /12345/com.tle.plugin/image.gif
		final int ind1 = pathInfo.indexOf('/', 1);
		final int ind2 = pathInfo.indexOf('/', ind1 + 1);
		if( ind1 <= 0 || ind2 <= 0 )
		{
			response.sendError(404);
			return;
		}

		response.setDateHeader("Expires", expires);
		response.setHeader("Cache-Control", maxAge);

		final String resourcePath = PathUtils.relativeUrlPath("", pathInfo.substring(ind2 + 1)).substring(1);
		final String filename = PathUtils.getFilenameFromFilepath(resourcePath);
		String mimeType = CONTENT_TYPES.get(PathUtils.extension(filename));

		if( mimeType == null )
		{
			mimeType = mimeService.getMimeTypeForFilename(resourcePath);
		}

		super.service(request, response, resourcePath, mimeType);
	}

	@Override
	public String getRootPath()
	{
		return "web/";
	}

	@Override
	public String getPluginId(HttpServletRequest request)
	{
		final String pathInfo = request.getPathInfo();

		final int ind1 = pathInfo.indexOf('/', 1);
		final int ind2 = pathInfo.indexOf('/', ind1 + 1);

		return pathInfo.substring(ind1 + 1, ind2);
	}
}
