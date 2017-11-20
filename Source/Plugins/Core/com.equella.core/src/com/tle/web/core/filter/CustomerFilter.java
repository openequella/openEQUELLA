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

package com.tle.web.core.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CustomerFilter extends AbstractWebFilter
{
	private static final Pattern REMOVE_VERSION_FROM_PATH = Pattern.compile("^p/r/[^/]+/(.*)$");
	private static final Pattern CSS_PROVIDED_BY_PLUGIN = Pattern.compile("^p/r/.+\\.css$", Pattern.CASE_INSENSITIVE);

	@Inject
	private FileSystemService fileService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	public void destroy()
	{
		// Ignore
	}

	public void init(FilterConfig fconfig)
	{
		// Ignore
	}

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response) throws IOException,
		ServletException
	{
		if( CurrentInstitution.get() != null )
		{
			String path = (request.getServletPath() + Strings.nullToEmpty(request.getPathInfo())).substring(1);

			// Disallow overriding of CSS files supplied by plugins. Clients
			// should always override CSS rules in customer.css
			if( CSS_PROVIDED_BY_PLUGIN.matcher(path).matches() )
			{
				return FilterResult.FILTER_CONTINUE;
			}

			// Remove the version number from the path which is there to
			// facilitate long expiry dates.
			final Matcher m = REMOVE_VERSION_FROM_PATH.matcher(path);
			if( m.matches() )
			{
				path = "p/r/" + m.group(1);
			}

			CustomisationFile customFile = new CustomisationFile();
			if( fileService.fileExists(customFile, path) )
			{
				String mimeType = mimeTypeService.getMimeTypeForFilename(path);
				FileContentStream contentStream = fileService.getContentStream(customFile, path, mimeType);
				contentStreamWriter.outputStream(request, response, contentStream);
				response.flushBuffer();
			}
		}
		return FilterResult.FILTER_CONTINUE;
	}
}
