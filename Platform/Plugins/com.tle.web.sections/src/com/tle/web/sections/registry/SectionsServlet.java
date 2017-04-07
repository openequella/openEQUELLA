package com.tle.web.sections.registry;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;

@Bind
public class SectionsServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private TreeRegistry treeRegistry;
	@Inject
	private SectionsController sectionsController;

	private String treepath;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		treepath = config.getInitParameter("treepath"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		MutableSectionInfo sectionInfo = null;
		String path = treepath;
		String servletPath = request.getServletPath();
		if( request.getParameter("$RESET$") != null ) //$NON-NLS-1$
		{
			treeRegistry.clearAll();
		}
		if( path == null )
		{
			path = servletPath;
		}
		SectionTree tree = treeRegistry.getTreeForPath(path, true);
		if( tree == null )
		{
			response.sendError(404);
		}
		else
		{
			sectionInfo = sectionsController.createInfo(tree, servletPath, request, response, null,
				request.getParameterMap(), Collections.singletonMap(SectionInfo.KEY_FROM_REQUEST, true));
			if( !sectionInfo.isRendered() )
			{
				sectionsController.execute(sectionInfo);
			}
		}
	}
}
