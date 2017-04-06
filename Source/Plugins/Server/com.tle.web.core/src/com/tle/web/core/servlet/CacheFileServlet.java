package com.tle.web.core.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.Check;
import com.tle.core.filesystem.CachedFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class CacheFileServlet extends AbstractIdPathServlet
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, String cacheUuid, String path)
		throws ServletException, IOException
	{
		String mimeType = request.getParameter("mimeType");
		if( Check.isEmpty(mimeType) )
		{
			mimeType = mimeService.getMimeTypeForFilename(path);
		}

		CachedFile cacheFile = new CachedFile(cacheUuid);
		FileContentStream stream = fileSystemService.getContentStream(cacheFile, path, mimeType);

		final String disposition = request.getParameter("disposition");
		if( !Check.isEmpty(disposition) )
		{
			stream.setContentDisposition(disposition);
		}

		contentStreamWriter.outputStream(request, response, stream);
	}
}
