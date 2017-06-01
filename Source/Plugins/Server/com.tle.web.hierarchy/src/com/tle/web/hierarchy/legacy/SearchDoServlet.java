/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
