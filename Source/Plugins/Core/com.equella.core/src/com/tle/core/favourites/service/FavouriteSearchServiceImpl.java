/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.dytech.edge.web.WebConstants;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.searching.Search;
import com.tle.common.searching.SortField;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.favourites.SearchFavouritesSearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.dao.FavouriteSearchDao;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("nls")
@Bind(FavouriteSearchService.class)
@Singleton
public class FavouriteSearchServiceImpl implements FavouriteSearchService, UserChangeListener {
  private static final String DEFAULT_ORDER = "dateModified";

  @Inject private FavouriteSearchDao dao;

  @Override
  @Transactional
  public void save(FavouriteSearch search) {
    dao.save(search);
  }

  @Override
  @Transactional
  public void deleteById(long id) {
    FavouriteSearch favouriteSearch = dao.getById(id);
    if (favouriteSearch != null) {
      dao.delete(favouriteSearch);
    }
  }

  @Override
  public void executeSearch(SectionInfo info, long id) {
    FavouriteSearch search = dao.getById(id);
    if (search != null) {

      String url = search.getUrl();
      // The value of 'url' starts with '/access' if the fav search was added in old oEQ versions,
      // which results in "no tree for xxx" error. Hence, remove "/access" if it exists.
      if (url != null
          && url.startsWith(WebConstants.ACCESS_PATH)
          && StringUtils.indexOfAny(
                  url,
                  new String[] {
                    WebConstants.SEARCHING_PAGE,
                    WebConstants.HIERARCHY_PAGE,
                    WebConstants.CLOUDSEARCH_PAGE
                  })
              > -1) {
        url = "/" + url.replaceFirst(WebConstants.ACCESS_PATH, "");
      }
      SectionInfo forward = info.createForwardForUri(url);
      info.forwardAsBookmark(forward);
    }
  }

  @Override
  @Transactional
  public SearchFavouritesSearchResults search(Search search, int offset, int perPage) {
    String userId = CurrentUser.getUserID();
    Institution institution = CurrentInstitution.get();

    SortField[] sortType = search.getSortFields();
    boolean reverse = search.isSortReversed();
    String sortField = sortType != null ? sortType[0].getField() : DEFAULT_ORDER;

    Date[] dates = search.getDateRange();

    int totalResults =
        (int) dao.count(Check.nullToEmpty(search.getQuery()), dates, userId, institution);
    List<FavouriteSearch> results =
        dao.search(
            search.getQuery(), dates, offset, perPage, sortField, reverse, userId, institution);

    return new SearchFavouritesSearchResults(results, results.size(), offset, totalResults);
  }

  @Override
  public List<FavouriteSearch> getSearchesForOwner(String userID, int maxResults) {
    return dao.findAllByCriteria(
        Order.desc("dateModified"),
        maxResults,
        Restrictions.eq("owner", userID),
        Restrictions.eq("institution", CurrentInstitution.get()));
  }

  @Override
  @Transactional
  public void userDeletedEvent(UserDeletedEvent event) {
    for (FavouriteSearch fs : getSearchesForOwner(event.getUserID(), -1)) {
      dao.delete(fs);
    }
  }

  @Override
  public void userEditedEvent(UserEditEvent event) {
    // We don't care
  }

  @Override
  @Transactional
  public void userIdChangedEvent(UserIdChangedEvent event) {
    for (FavouriteSearch fs : getSearchesForOwner(event.getFromUserId(), -1)) {
      fs.setOwner(event.getToUserId());
      dao.update(fs);
    }
  }
}
