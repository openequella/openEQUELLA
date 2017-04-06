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