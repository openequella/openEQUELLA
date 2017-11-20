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

package com.tle.web.notification.section;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.search.feeds.FeedServlet;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

/**
 * Re-purposed RssFeedDialog. This class should probably disappear in the future
 * and the StandardShareSearchQuerySection or similar be refactored and used
 * instead
 */
@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class RssFeedSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private FeedServlet feedServlet;

	@Component(name = "r")
	private Link rssLink;
	@Component(name = "a")
	private Link atomLink;
	@TreeLookup
	private AbstractRootSearchSection<?> rootSearch;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_SHARE);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		InfoBookmark bookmark = rootSearch.getPermanentUrl(context);
		setupFeeds(bookmark, context);
		return viewFactory.createResult("rss.ftl", this);
	}

	private void setupFeeds(InfoBookmark bookmark, RenderContext context)
	{
		rssLink.setBookmark(context,
			new BookmarkAndModify(bookmark, feedServlet.getModifier(context, "rss_2.0", FeedServlet.AUTH_BASIC)));
		atomLink.setBookmark(context,
			new BookmarkAndModify(bookmark, feedServlet.getModifier(context, "atom_1.0", FeedServlet.AUTH_BASIC)));
	}

	public Link getRssLink()
	{
		return rssLink;
	}

	public Link getAtomLink()
	{
		return atomLink;
	}
}