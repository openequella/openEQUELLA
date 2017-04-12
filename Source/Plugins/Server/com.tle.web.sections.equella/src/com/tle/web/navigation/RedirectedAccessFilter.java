package com.tle.web.navigation;

import java.io.IOException;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.web.WebConstants;
import com.google.common.base.Strings;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilter;
import com.tle.web.sections.SectionUtils;

/**
 * Originates with EQ-2212/EQ-2203. The "access" in URLs like
 * ../access/hierarchy.do?... is removed from the URL, hence where it is so
 * present, we redirect to the truncated version (in a sense, as if "access/"
 * was stripped out from the path)
 * 
 * @author larry
 */
@Bind
@Singleton
public class RedirectedAccessFilter implements WebFilter
{
	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response) throws IOException,
		ServletException
	{
		FilterResult result = new FilterResult(request);

		String contextPath = Strings.nullToEmpty(request.getContextPath());
		String originalServletPath = (request.getServletPath() + Strings.nullToEmpty(request.getPathInfo()))
			.substring(1);
		String accessNoLeadingSlash = WebConstants.ACCESS_PATH.substring(1);
		if( originalServletPath.endsWith(accessNoLeadingSlash + WebConstants.DASHBOARD_PAGE)
			|| originalServletPath.endsWith(accessNoLeadingSlash + WebConstants.SEARCHING_PAGE)
			|| originalServletPath.endsWith(accessNoLeadingSlash + WebConstants.CLOUDSEARCH_PAGE)
			|| originalServletPath.endsWith(accessNoLeadingSlash + WebConstants.HIERARCHY_PAGE)
			|| originalServletPath.endsWith(accessNoLeadingSlash + "logonnotice.do") )
		{
			String truncatedPath = originalServletPath.substring(WebConstants.ACCESS_PATH.length() - 2);
			String reconstitutedPath = contextPath + truncatedPath;
			Map<String, String[]> params = request.getParameterMap();
			String query = SectionUtils.getParameterString(SectionUtils.getParameterNameValues(params, false));
			if( !Check.isEmpty(query) )
			{
				reconstitutedPath += '?' + query;
			}
			response.sendRedirect(reconstitutedPath);
			result.setStop(true);
		}
		return result;
	}
}
