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

package com.tle.web.core.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

@Bind
@Singleton
public class EntityAttachmentsServlet extends AbstractIdPathServlet
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, String entityId, String path)
		throws ServletException, IOException
	{
		EntityFile entFile = new EntityFile(Long.parseLong(entityId));
		FileContentStream stream = fileSystemService.getContentStream(entFile, path,
			mimeService.getMimeTypeForFilename(path));
		contentStreamWriter.outputStream(request, response, stream);
	}
}
