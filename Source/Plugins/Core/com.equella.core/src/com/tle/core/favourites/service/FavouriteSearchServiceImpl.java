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
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.template.RenderNewTemplate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
  public FavouriteSearch save(FavouriteSearch search) {
    Long id = dao.save(search);
    return dao.getById(id);
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
    Optional<FavouriteSearch> search = Optional.ofNullable(dao.getById(id));
    Optional<String> url = search.map(FavouriteSearch::getUrl);
    if (!search.isPresent() || !url.isPresent() || url.get().isEmpty()) {
      throw new SectionsRuntimeException("Favourite search with id " + id + " is invalid.");
    }

    String path = url.get();
    // If new Search UI is enabled then navigate to it by calling 'forwardToUrl'.
    // If new Search UI is disabled but the search was added from new Search UI, throw an error.
    // Else do however it used to do.
    if (RenderNewTemplate.isNewSearchPageEnabled()) {
      // Remove the last '/' from 'CurrentInstitution.get().getUrl()' and then construct a full
      // path.
      String fullPath = StringUtils.removeEnd(CurrentInstitution.get().getUrl(), "/") + path;
      info.forwardToUrl(fullPath);
      return;
    } else if (path.contains("/page/search")) {
      throw new SectionsRuntimeException("This favourite search is only available in New UI mode.");
    }

    // When user favourites a normal search, cloud search or hierarchy search,
    // the value of 'url' starts with '/access' if the fav search is added in old oEQ versions,
    // which results in "no tree for xxx" error. Hence, remove "/access" if it exists.
    if (path.startsWith(WebConstants.ACCESS_PATH)
        && StringUtils.indexOfAny(
                path,
                new String[] {
                  WebConstants.SEARCHING_PAGE,
                  WebConstants.HIERARCHY_PAGE,
                  WebConstants.CLOUDSEARCH_PAGE
                })
            > -1) {
      path = "/" + path.replaceFirst(WebConstants.ACCESS_PATH, "");
    }
    SectionInfo forward = info.createForwardForUri(path);
    info.forwardAsBookmark(forward);
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
