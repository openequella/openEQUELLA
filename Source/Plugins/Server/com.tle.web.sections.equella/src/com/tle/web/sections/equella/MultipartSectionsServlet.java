package com.tle.web.sections.equella;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.sections.registry.SectionsServlet;
import com.tle.web.upload.TLEMultipartResolver;

@Bind
public class MultipartSectionsServlet extends SectionsServlet
{
	@Inject
	private TLEMultipartResolver multipartResolver;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		if( multipartResolver.isMultipart(request) )
		{
			request = multipartResolver.resolveMultipart(request);
		}
		super.service(request, response);
	}
}
