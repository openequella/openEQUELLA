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
import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid;
import com.tle.web.integration.IntegrationSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.template.RenderNewTemplate;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
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

  /**
   * CAUTION: it's known that the code below can cause issues in Selection Session. For example, a
   * favourite normal search added in New UI will result in missing Old UI components (e.g. top
   * menus) in Selection Session. This is mainly because of the mix use of Old UI and New UI in
   * Selection Session. The Switch of Enable new search page also makes the code more complicated.
   *
   * <p>While working on the Hierarchy new UI, it's agreed that we must add some workaround to make
   * favourite Hierarchy searches work properly in both Selection Session and native OEQ, and
   * implement a proper fix for all the favourite searches in the future. The temporary fix requires
   * three steps:
   * <li>1. If New UI is turned off, use `info.forwardToUrl` to redirect the page. This can make
   *     sure the URL path and query parameters are kept and avoid `no tree for xxx` error.
   * <li>2. In Selection Session, if the favourite Hierarchy search is added in New UI, change its
   *     path from `/page/hierarchy` to `/hierarchy.do`.
   * <li>3. In Selection Session, add Session ID and Integration ID to the query parameters.
   * <li>4. Throw `SectionsRuntimeException` if the favourite is for New Search UI but New Search UI
   *     is disabled, or if it's for New Hierarchy UI but New UI is disabled.
   */
  @Override
  public void executeSearch(SectionInfo info, long id) {
    Optional<FavouriteSearch> search = Optional.ofNullable(dao.getById(id));
    Optional<String> url = search.map(FavouriteSearch::getUrl);
    if (search.isEmpty() || url.isEmpty() || url.get().isEmpty()) {
      throw new SectionsRuntimeException("Favourite search with id " + id + " is invalid.");
    }

    String path = url.get();
    boolean isFavNewSearch = path.contains("/page/search");
    boolean isFavNewHierarchy = path.contains("/page/hierarchy");
    boolean isFavOldHierarchy = path.contains("/hierarchy.do");
    boolean isNewUIEnabled = RenderNewTemplate.isNewUIEnabled();

    if ((!RenderNewTemplate.isNewSearchPageEnabled() && isFavNewSearch)
        || (!isNewUIEnabled && isFavNewHierarchy)) {
      throw new SectionsRuntimeException("This favourite search is only available in New UI mode.");
    }

    if (isNewUIEnabled) {
      try {
        URIBuilder uriBuilder = new URIBuilder(path);
        // Add Selection Session ID to the query parameters and process URL if necessary.
        RootSelectionSection selectionSection = info.lookupSection(RootSelectionSection.class);
        Optional.ofNullable(selectionSection)
            .ifPresent(
                section -> {
                  uriBuilder.addParameter(
                      RootSelectionSection.STATE_ID_PARAM, section.getSessionId(info));
                  if (isFavNewHierarchy) {
                    processFavNewHierarchyURL(uriBuilder);
                  }
                });

        // Add Integration ID to the query parameters.
        IntegrationSection integrationSection = info.lookupSection(IntegrationSection.class);
        Optional.ofNullable(integrationSection)
            .ifPresent(
                section ->
                    uriBuilder.addParameter(
                        IntegrationSection.INTEG_ID_PARAM, section.getStateId(info)));
        // Replace old compound UUID with new format compound UUID,
        if (isFavOldHierarchy) {
          getQueryParam(uriBuilder, "topic")
              .ifPresent(
                  legacyUuid -> {
                    String newCompoundUuid =
                        HierarchyCompoundUuid.apply(legacyUuid, true).toString(false);
                    uriBuilder.setParameter("topic", newCompoundUuid);
                  });
        }

        String fullPath =
            StringUtils.removeEnd(CurrentInstitution.get().getUrl(), "/")
                + uriBuilder.build().toString();
        info.forwardToUrl(fullPath);
      } catch (URISyntaxException e) {
        throw new SectionsRuntimeException("Failed to build favourite search URL: " + path, e);
      }
    } else {
      // When user favourites a normal search or hierarchy search in older oEQ versions, the value
      // of 'url' starts with '/access'.
      // This will result in "no tree for xxx" error. Hence, remove "/access" if it exists.
      if (path.startsWith(WebConstants.ACCESS_PATH)
          && StringUtils.indexOfAny(
                  path, new String[] {WebConstants.SEARCHING_PAGE, WebConstants.HIERARCHY_PAGE})
              > -1) {
        path = "/" + path.replaceFirst(WebConstants.ACCESS_PATH, "");
      }
      SectionInfo forward = info.createForwardForUri(path);
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

  // Change the new Hierarchy page URL to the old one with query string 'topic'.
  private void processFavNewHierarchyURL(URIBuilder uriBuilder) {
    // Regex to extract topic ID from the new Hierarchy page URL.
    // It will extract all text after '/page/hierarchy/',
    // but maybe it's enough for now since `uriBuilder.getPath()` will return the path without
    // parameters after question mark.
    Pattern p = Pattern.compile("^/page/hierarchy/(.+)"); // Put the topic ID in the first group.
    Matcher m = p.matcher(uriBuilder.getPath());
    if (m.find()) {
      String topicID = m.group(1);
      uriBuilder.addParameter("topic", topicID);
      uriBuilder.setPath("/hierarchy.do");
    } else {
      throw new SectionsRuntimeException(
          "Invalid favourite hierarchy search URL: missing topic ID");
    }
  }

  // Extract query parameter from URL.
  private static Optional<String> getQueryParam(URIBuilder uriBuilder, String param) {
    return uriBuilder.getQueryParams().stream()
        .filter(p -> p.getName().equals(param))
        .map(NameValuePair::getValue)
        .findFirst();
  }
}
