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
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.UrlService;
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
	private UrlService urlService;

	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
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
		response.sendRedirect(urlService.institutionalise(resources.url(PathUtils.urlPath("scripts/tinymce/", path))));
	}
}
