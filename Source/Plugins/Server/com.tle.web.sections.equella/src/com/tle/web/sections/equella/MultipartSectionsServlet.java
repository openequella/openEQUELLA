package com.tle.web.sections.equella;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.sections.registry.SectionsServlet;

@Bind
@MultipartConfig
public class MultipartSectionsServlet extends SectionsServlet
{

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		super.service(request, response);
	}
}
