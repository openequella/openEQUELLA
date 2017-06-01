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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.search.feeds.FeedServlet;
import com.tle.web.searching.section.RootSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.InfoBookmark;

@Bind
@Singleton
@SuppressWarnings("nls")
public class LegacyRSSServlet extends LegacySearchServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private FeedServlet feedServlet;

	@Override
	protected String getCollectionUuidsParam(HttpServletRequest request)
	{
		String idents = request.getParameter("itemdefUuids"); //$NON-NLS-1$
		if( idents != null )
		{
			String[] pitemdefs = idents.split(":"); //$NON-NLS-1$
			if( pitemdefs.length > 0 )
			{
				return pitemdefs[0];
			}
		}
		return null;
	}

	@Override
	protected String getPowerSearchParam(HttpServletRequest request)
	{
		return request.getParameter("powersearch");
	}

	@Override
	protected String getPowerXmlParam(HttpServletRequest request)
	{
		return request.getParameter("powerdoc");
	}

	@Override
	protected String getQueryString(HttpServletRequest request)
	{
		return request.getParameter("query");
	}

	@Override
	protected String getSortParam(HttpServletRequest request)
	{
		return request.getParameter("sorttype");
	}

	@Override
	protected String getTopicParameter(HttpServletRequest request)
	{
		return request.getParameter("topic");
	}

	@Override
	protected void redirect(HttpServletRequest request, HttpServletResponse response, SectionInfo info)
		throws IOException
	{
		RootSearchSection rootSearch = info.lookupSection(RootSearchSection.class);
		InfoBookmark bookmark = rootSearch.getPermanentUrl(info);
		String feedType = request.getParameter("type");
		response.sendRedirect(new BookmarkAndModify(bookmark, feedServlet.getModifier(info, feedType,
			request.getParameter("auth"))).getHref());

	}
}
