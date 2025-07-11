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

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.favourites.dao.BookmarkDao;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.exceptions.AccessDeniedException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("nls")
@Bind(BookmarkService.class)
@Singleton
public class BookmarkServiceImpl implements BookmarkService, UserChangeListener {
  @Inject private BookmarkDao dao;
  @Inject private ItemService itemService;
  @Inject private ItemOperationFactory workflowFactory;

  @Override
  public Bookmark getByItem(ItemKey itemId) {
    return dao.getByItemAndUserId(CurrentUser.getUserID(), itemId);
  }

  @Override
  public Bookmark getById(long id) {
    return dao.findById(id);
  }

  @Override
  @Transactional
  public Bookmark add(Item item, Set<String> tags, boolean latest) {
    Bookmark bookmark = new Bookmark();
    bookmark.setItem(item);
    bookmark.setKeywords(tags);
    bookmark.setOwner(CurrentUser.getUserID());
    bookmark.setInstitution(CurrentInstitution.get());
    bookmark.setDateModified(new Date());
    bookmark.setAlwaysLatest(latest);
    dao.save(bookmark);

    itemService.operation(bookmark.getItem().getItemId(), workflowFactory.reindexOnly(true));
    return bookmark;
  }

  @Override
  @Transactional
  public void delete(long id) {
    Bookmark bookmark = dao.findById(id);
    delete(bookmark);
  }

  @Override
  @Transactional
  public void deleteIfOwned(long id) {
    Bookmark bookmark =
        Optional.ofNullable(getById(id))
            .orElseThrow(() -> new NotFoundException("No Bookmark matching ID: " + id));

    if (!isOwner(bookmark)) {
      throw new AccessDeniedException("You are not the owner of the favourite item with ID: " + id);
    }

    delete(bookmark);
  }

  @Override
  @Transactional
  public List<Item> filterNonBookmarkedItems(Collection<Item> items) {
    return dao.filterNonBookmarkedItems(items);
  }

  @Override
  @Transactional
  public Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items) {
    return dao.getBookmarksForItems(items, CurrentUser.getUserID());
  }

  @Override
  public List<Bookmark> getBookmarksForOwner(String ownerUuid, int maxResults) {
    return dao.findAllByCriteria(
        Order.desc("dateModified"),
        maxResults,
        Restrictions.eq("owner", ownerUuid),
        Restrictions.eq("institution", CurrentInstitution.get()));
  }

  @Override
  @Transactional
  public void userDeletedEvent(UserDeletedEvent event) {
    dao.deleteAllForUser(event.getUserID());
  }

  @Override
  @Transactional
  public void userIdChangedEvent(UserIdChangedEvent event) {
    dao.changeOwnership(event.getFromUserId(), event.getToUserId());
  }

  @Override
  public void userEditedEvent(UserEditEvent event) {
    // Nothing to do here
  }

  @Override
  public boolean isOwner(Bookmark favouriteItem) {
    String userId = CurrentUser.getUserID();
    return favouriteItem.getOwner().equals(userId);
  }

  private void delete(Bookmark favouriteItem) {
    dao.delete(favouriteItem);
    itemService.operation(favouriteItem.getItem().getItemId(), workflowFactory.reindexOnly(true));
  }
}
