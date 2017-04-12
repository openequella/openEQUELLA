package com.tle.web.core.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.filesystem.PublicFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

@Bind
@Singleton
public class PublicFileServlet extends AbstractIdPathServlet
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response, String uuid, String path)
		throws ServletException, IOException
	{
		PublicFile pubFile = new PublicFile(uuid);
		FileContentStream stream = fileSystemService.getContentStream(pubFile, path,
			mimeService.getMimeTypeForFilename(path));
		contentStreamWriter.outputStream(request, response, stream);
	}
}
