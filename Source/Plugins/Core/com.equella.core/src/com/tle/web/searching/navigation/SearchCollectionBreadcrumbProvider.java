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

package com.tle.web.searching.navigation;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.security.TLEAclManager;
import com.tle.web.navigation.BreadcrumbProvider;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SearchCollectionBreadcrumbProvider implements BreadcrumbProvider
{
	@PlugKey("breadcrumb.collection.untitled")
	private static Label LABEL_UNTITLED;
	@PlugKey("searching.breadcrumb.title")
	private static Label BREADCRUMB_TITLE;

	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private TLEAclManager aclService;

	@SuppressWarnings("nls")
	@Override
	public TagState getBreadcrumb(SectionInfo info, Map<String, ?> params)
	{
		final String collectionUuid = (String) params.get("collectionUuid");
		final SectionInfo fwd = info.createForward("/searching.do");
		final SearchQuerySection search = fwd.lookupSection(SearchQuerySection.class);
		search.setCollection(fwd, collectionUuid);

		final ItemDefinition collection = collectionService.getByUuid(collectionUuid);
		final Label label = new BundleLabel(collection.getName(), LABEL_UNTITLED, bundleCache);

		if( Check
			.isEmpty(aclService.filterNonGrantedPrivileges(collection, Collections.singleton("SEARCH_COLLECTION"))) )
		{
			final HtmlComponentState collectionSpan = new HtmlComponentState();
			collectionSpan.setDefaultRenderer("span");
			collectionSpan.setLabel(label);
			return collectionSpan;
		}

		final HtmlLinkState collectionLink = new HtmlLinkState();
		collectionLink.setLabel(label);

		// "..page=<n>.." can be misleading (when the original search which
		// rendered the item we are now viewing was an "All Resources" search),
		// so remove it from the breadcrumb. (Redmine #6041). Other filters
		// (query-string, date-range etc) maintain their persistence
		// independently of the breadcrumb. NB: note that if the "q" attribute
		// is removed, the page reloads with an empty result set, so we're
		// obliged to leave that one at least (as well as the collection "in").
		Bookmark unpagedBookmark = new BookmarkAndModify(fwd, new BookmarkModifier()
		{
			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				bookmarkState.remove("page");
			}
		});

		collectionLink.setBookmark(unpagedBookmark);
		collectionLink.setRel("parent");
		collectionLink.setTitle(BREADCRUMB_TITLE);
		return collectionLink;
	}
}
