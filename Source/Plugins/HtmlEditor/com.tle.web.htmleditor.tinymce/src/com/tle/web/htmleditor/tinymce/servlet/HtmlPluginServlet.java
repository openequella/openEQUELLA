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

package com.tle.web.htmleditor.tinymce.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.PathUtils;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

/**
 * Hosted at htmlplugin
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class HtmlPluginServlet extends HttpServlet
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(HtmlPluginServlet.class);

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private InstitutionService institutionService;

	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String path = request.getPathInfo();
		if( path.startsWith("/") )
		{
			path = path.substring(1);
		}

		int firstSlash = path.indexOf('/');
		if( firstSlash < 0 )
		{
			// could be a tinymce file
			redirectToTinyMceFiles(response, path);
			return;
		}
		else
		{
			final String firstPart = path.substring(0, firstSlash);
			final long entityId;
			try
			{
				entityId = Long.parseLong(firstPart);
			}
			catch( NumberFormatException nfe )
			{
				// could be a tinymce file
				redirectToTinyMceFiles(response, path);
				return;
			}

			final EntityFile entFile = new EntityFile(entityId);
			final String relPath = path.substring(firstSlash + 1);
			final FileContentStream stream = fileSystemService.getContentStream(entFile, relPath,
				mimeService.getMimeTypeForFilename(relPath));
			contentStreamWriter.outputStream(request, response, stream);
		}
	}

	private void redirectToTinyMceFiles(HttpServletResponse response, String path) throws IOException
	{
		response.sendRedirect(
			institutionService.institutionalise(resources.url(PathUtils.urlPath("scripts/tinymce/", path))));
	}
}
