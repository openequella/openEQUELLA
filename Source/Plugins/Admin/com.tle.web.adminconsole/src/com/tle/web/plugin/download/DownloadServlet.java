package com.tle.web.plugin.download;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

@Bind
@Singleton
public class DownloadServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private PluginDownloadService pluginDownloadService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		String pathInfo = request.getPathInfo().substring(1);
		File file = pluginDownloadService.getFileForJar(pathInfo);
		if( file != null )
		{
			FileContentStream stream = new FileContentStream(file, file.getName(), "application/java-archive"); //$NON-NLS-1$
			contentStreamWriter.outputStream(request, response, stream);
		}
		else
		{
			response.sendError(404);
		}
	}
}
