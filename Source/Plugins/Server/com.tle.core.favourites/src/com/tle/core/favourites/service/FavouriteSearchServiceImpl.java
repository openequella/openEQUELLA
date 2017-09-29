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

package com.tle.core.favourites.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.searching.Search;
import com.tle.common.searching.SortField;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.favourites.SearchFavouritesSearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.dao.FavouriteSearchDao;
import com.tle.core.guice.Bind;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;

@SuppressWarnings("nls")
@Bind(FavouriteSearchService.class)
@Singleton
public class FavouriteSearchServiceImpl implements FavouriteSearchService, UserChangeListener
{
	private static final String DEFAULT_ORDER = "dateModified";

	@Inject
	private FavouriteSearchDao dao;
	@Inject
	private SectionsController controller;

	@Override
	@Transactional
	public void save(FavouriteSearch search)
	{
		dao.save(search);
	}

	@Override
	@Transactional
	public void deleteById(long id)
	{
		FavouriteSearch favouriteSearch = dao.getById(id);
		if( favouriteSearch != null )
		{
			dao.delete(favouriteSearch);
		}
	}

	@Override
	public void executeSearch(SectionInfo info, long id)
	{
		FavouriteSearch search = dao.getById(id);
		if( search != null )
		{
			SectionInfo forward = controller.createForward(info, search.getUrl());
			controller.forward(info, forward);
		}

	}

	@Override
	@Transactional
	public SearchFavouritesSearchResults search(Search search, int offset, int perPage)
	{
		String userId = CurrentUser.getUserID();
		Institution institution = CurrentInstitution.get();

		SortField[] sortType = search.getSortFields();
		boolean reverse = search.isSortReversed();
		String sortField = sortType != null ? sortType[0].getField() : DEFAULT_ORDER;

		Date[] dates = search.getDateRange();

		int totalResults = (int) dao.count(Check.nullToEmpty(search.getQuery()), dates, userId, institution);
		List<FavouriteSearch> results = dao.search(search.getQuery(), dates, offset, perPage, sortField, reverse,
			userId, institution);

		return new SearchFavouritesSearchResults(results, results.size(), offset, totalResults);
	}

	@Override
	public List<FavouriteSearch> getSearchesForOwner(String userID, int maxResults)
	{
		return dao.findAllByCriteria(Order.desc("dateModified"), maxResults, Restrictions.eq("owner", userID),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		for( FavouriteSearch fs : getSearchesForOwner(event.getUserID(), -1) )
		{
			dao.delete(fs);
		}
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// We don't care
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		for( FavouriteSearch fs : getSearchesForOwner(event.getFromUserId(), -1) )
		{
			fs.setOwner(event.getToUserId());
			dao.update(fs);
		}
	}
}
