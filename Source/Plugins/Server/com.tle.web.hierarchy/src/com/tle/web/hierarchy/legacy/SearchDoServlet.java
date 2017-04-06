package com.tle.web.hierarchy.legacy;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.InfoBookmark;

@Bind
@Singleton
@SuppressWarnings("nls")
public class SearchDoServlet extends LegacySearchServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected String getCollectionUuidsParam(HttpServletRequest request)
	{
		return request.getParameter("guided.coluuid"); //$NON-NLS-1$
	}

	@Override
	protected String getPowerSearchParam(HttpServletRequest request)
	{
		return request.getParameter("pow_p");
	}

	@Override
	protected String getPowerXmlParam(HttpServletRequest request)
	{
		return request.getParameter("pow.x");
	}

	@Override
	protected String getQueryString(HttpServletRequest request)
	{
		return request.getParameter("qs.q");
	}

	@Override
	protected String getSortParam(HttpServletRequest request)
	{
		return request.getParameter("sort_s");
	}

	@Override
	protected String getTopicParameter(HttpServletRequest request)
	{
		return request.getParameter("hier.topic");
	}

	@Override
	protected void redirect(HttpServletRequest request, HttpServletResponse response, SectionInfo info)
		throws IOException
	{
		response.sendRedirect(new InfoBookmark(info).getHref());
	}

}
