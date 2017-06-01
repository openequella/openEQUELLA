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

package com.tle.web.search.base;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.PublicBookmarkFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

public abstract class ContextableSearchSection<M extends ContextableSearchSection.Model>
	extends
		AbstractRootSearchSection<M> implements PublicBookmarkFactory, AfterParametersListener, ReadyToRespondListener
{
	public static final String HISTORYURL_CONTEXT = "css-historyUrl"; //$NON-NLS-1$

	@Inject
	private UserSessionService userSessionService;
	@Inject
	protected SelectionService selectionService;
	@TreeLookup(mandatory = false)
	private AbstractQuerySection<?, ?> querySection;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		// tree.setAttribute(PublicBookmarkFactory.class, this);
	}

	@Override
	public Bookmark getPublicBookmark(SectionInfo info)
	{
		BookmarkEvent bookmarkEvent = new BookmarkEvent(BookmarkEvent.CONTEXT_SESSION,
			BookmarkEvent.CONTEXT_BROWSERURL, HISTORYURL_CONTEXT);
		return new InfoBookmark(info, bookmarkEvent);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setUpdateContext(true);
		getModel(context).setCourseSelectionSession(isCourseSelectionSession(context));
		return super.renderHtml(context);
	}

	@Override
	public void afterParameters(SectionInfo info, ParametersEvent event)
	{
		M model = getModel(info);
		if( !event.isInitial() || info.getBooleanAttribute(SectionInfo.KEY_FOR_URLS_ONLY) )
		{
			return;
		}
		if( hasContextBeenSpecified(info) || model.isUpdateContext() )
		{
			model.setUpdateContext(true);
			return;
		}
		Map<String, String[]> searchContext;
		SelectionSession selectionSession = selectionService.getCurrentSession(info);
		if( selectionSession == null )
		{
			searchContext = userSessionService.getAttribute(getSessionKey());
		}
		else
		{
			searchContext = selectionSession.getSearchContext(getSessionKey());
		}
		if( searchContext != null )
		{
			model.setContext(searchContext);
			Map<String, String[]> context = new HashMap<String, String[]>(searchContext);
			context.keySet().removeAll(event.getParameterNames());
			info.processEvent(new ParametersEvent(context, false));
		}
	}

	private boolean isCourseSelectionSession(SectionInfo info)
	{
		SelectionSession currentSession = selectionService.getCurrentSession(info);
		if( currentSession != null && currentSession.getLayout() == Layout.COURSE )
		{
			return true;
		}
		return false;
	}

	protected boolean hasContextBeenSpecified(SectionInfo info)
	{
		return querySection.getQueryField().getValue(info) != null;
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		if( redirect || getModel(info).isUpdateContext() )
		{
			final Map<String, String[]> searchContext = buildSearchContext(info);
			final SelectionSession selectionSession = selectionService.getCurrentSession(info);
			if( selectionSession == null )
			{
				userSessionService.setAttribute(getSessionKey(), searchContext);
			}
			else
			{
				selectionSession.setSearchContext(getSessionKey(), searchContext);
			}
		}
	}

	protected Map<String, String[]> buildSearchContext(SectionInfo info)
	{
		final BookmarkEvent bookmarkEvent = new BookmarkEvent();
		bookmarkEvent.setIgnoredContexts(BookmarkEvent.CONTEXT_SESSION, BookmarkEvent.CONTEXT_BROWSERURL);
		info.processEvent(bookmarkEvent);
		return bookmarkEvent.getBookmarkState();
	}

	protected abstract String getSessionKey();

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model extends AbstractRootSearchSection.Model
	{
		@Bookmarked(stateful = false, parameter = "uc")
		private boolean updateContext;
		private Map<String, String[]> context;
		private boolean courseSelectionSession;

		public boolean isUpdateContext()
		{
			return updateContext;
		}

		public void setUpdateContext(boolean updateContext)
		{
			this.updateContext = updateContext;
		}

		public Map<String, String[]> getContext()
		{
			return context;
		}

		public void setContext(Map<String, String[]> context)
		{
			this.context = context;
		}

		public boolean isCourseSelectionSession()
		{
			return courseSelectionSession;
		}

		public void setCourseSelectionSession(boolean courseSelectionSession)
		{
			this.courseSelectionSession = courseSelectionSession;
		}
	}
}
