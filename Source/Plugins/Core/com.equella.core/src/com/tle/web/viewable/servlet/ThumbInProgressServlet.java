/*
 * Copyright 2019 Apereo
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

package com.tle.web.viewable.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.java.plugin.util.IoUtil;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.web.stream.ContentStream;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;
import com.tle.web.stream.URLContentStream;

/**
 * This servlet serves up placeholder thumbnails for when a thumbnail request is
 * queued up and hasn't completed yet. The standard thumbs servlet will redirect
 * here if appropriate. The URL format is /thumbprogress/{type}. Where type =
 * standard OR gallery_thumbnail. Anything else is a 404.
 * 
 * @author aholland
 */
@NonNullByDefault
@Bind
@Singleton
@SuppressWarnings("nls")
public class ThumbInProgressServlet extends HttpServlet
{
	public static final String STANDARD = "standard";
	public static final String GALLERY_THUMBNAIL = "gallery_thumbnail";

	private static final long serialVersionUID = 1L;

	private static final String[] PLACEHOLDERS = {"nothumb.jpeg", "nothumb_135.jpeg"};

	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		String path = request.getPathInfo();
		if( path.startsWith("/") )
		{
			path = Utils.safeSubstring(path, 1);
		}

		final int i;
		if( STANDARD.equals(path) )
		{
			i = 0;
		}
		else if( GALLERY_THUMBNAIL.equals(path) )
		{
			i = 1;
		}
		else
		{
			response.sendError(404);
			return;
		}

		final String filename = PLACEHOLDERS[i];
		final URL res = new URL(ThumbInProgressServlet.class.getClassLoader().getResource("web/images/"), filename);
		final File file = IoUtil.url2file(res);

		ContentStream stream;
		if( file != null )
		{
			stream = new FileContentStream(file, filename, "image/jpeg");
		}
		else
		{
			stream = new URLContentStream(res, filename, "image/jpeg");
		}
		stream.setCacheControl("max-age=31556926");
		contentStreamWriter.outputStream(request, response, stream);
	}
}
