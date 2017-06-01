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

package com.tle.web.hierarchy.migration;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.UserPreference;
import com.tle.common.Check;
import com.tle.common.SavedSearch;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.guice.Bind;
import com.tle.web.hierarchy.section.RootHierarchySection;
import com.tle.web.hierarchy.section.TopicDisplaySection;
import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.search.filter.FilterByKeywordSection;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.searching.section.RootSearchSection;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.generic.InfoBookmark;

@Bind
@Singleton
public class SavedSearchConverter
{
	@Inject
	private SectionsController sectionsController;

	public FavouriteSearch convertSavedSearch(Institution institution, UserPreference pref, SavedSearch ss)
	{
		// Basic (Name, Date, Owner)
		FavouriteSearch fs = new FavouriteSearch();
		fs.setName(ss.getName());
		fs.setQuery(ss.getQuery());
		fs.setDateModified(new Date()); // No date available.
		fs.setOwner(pref.getKey().getUserID());
		fs.setInstitution(institution);

		// Set URL
		fs.setUrl(buildURL(ss));

		return fs;
	}

	private String buildURL(SavedSearch ss)
	{
		SectionInfo info = null;
		boolean isHier = !Check.isEmpty(ss.getHierarchyTopicUuid());

		if( !isHier )
		{
			info = sectionsController.createForward(RootSearchSection.SEARCHURL);
			SearchQuerySection sqs = info.lookupSection(SearchQuerySection.class);

			sqs.getQueryField().setValue(info, ss.getQuery());

			// Advanced Search Stuff
			if( !Check.isEmpty(ss.getPowersearchUuid()) )
			{
				sqs.setPowerSearch(info, ss.getPowersearchUuid(), ss.getPowersearchxml());
			}

			// Collection(s) - just use the first one...no guided
			if( !Check.isEmpty(ss.getCollections()) )
			{
				sqs.setCollection(info, ss.getCollections().iterator().next());
			}
		}
		else
		{
			info = sectionsController.createForward(RootHierarchySection.HIERARCHYURL);

			// Hierarchy Topic
			TopicDisplaySection tds = info.lookupSection(TopicDisplaySection.class);
			tds.getModel(info).setTopicId(ss.getHierarchyTopicUuid());

			// Filter
			FilterByKeywordSection fbkws = info.lookupSection(FilterByKeywordSection.class);
			fbkws.setQuery(info, ss.getQuery());
		}

		// Sorting
		AbstractSortOptionsSection sos = info.lookupSection(AbstractSortOptionsSection.class);
		sos.getSortOptions().setSelectedStringValue(info, ss.getSort());

		// Return the URL
		AbstractRootSearchSection<?> rootSection = info.lookupSection(AbstractRootSearchSection.class);
		InfoBookmark bookmark = rootSection.getPermanentUrl(info);
		String url = String.format("%s?%s", info.getAttribute(SectionInfo.KEY_PATH), bookmark //$NON-NLS-1$
			.getQuery());

		return url;
	}
}
