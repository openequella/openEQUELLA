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

package com.tle.core.favourites.dao;

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.item.dao.ItemDaoExtension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("nls")
@Bind(BookmarkDao.class)
@Singleton
public class BookmarkDaoImpl extends GenericInstitionalDaoImpl<Bookmark, Long>
    implements BookmarkDao, ItemDaoExtension {
  public BookmarkDaoImpl() {
    super(Bookmark.class);
  }

  @Override
  public Bookmark findById(long id) {
    List<Bookmark> list =
        (List<Bookmark>)
            getHibernateTemplate()
                .find(
                    "FROM Bookmark WHERE id = ?0 AND institution = ?1",
                    new Object[] {id, CurrentInstitution.get()});
    return list.isEmpty() ? null : list.get(0);
  }

  @Override
  public Bookmark getByItemAndUserId(String userId, ItemKey itemId) {
    List<Bookmark> find =
        (List<Bookmark>)
            getHibernateTemplate()
                .find(
                    "FROM Bookmark WHERE owner = ?0 AND item.uuid = ?1 AND item.version = ?2 AND"
                        + " institution = ?3",
                    new Object[] {
                      userId, itemId.getUuid(), itemId.getVersion(), CurrentInstitution.get()
                    });
    return find.isEmpty() ? null : find.get(0);
  }

  @Override
  public List<Bookmark> listAll() {
    return (List<Bookmark>)
        getHibernateTemplate()
            .find("FROM Bookmark WHERE institution = ?0", new Object[] {CurrentInstitution.get()});
  }

  @Override
  public void deleteAll() {
    for (Bookmark bookmark : listAll()) {
      delete(bookmark);
    }
  }

  @Override
  public void deleteAllForUser(final String user) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) {
                Query q =
                    session.createQuery(
                        "FROM Bookmark WHERE owner = :owner AND institution = :institution");
                q.setParameter("owner", user);
                q.setParameter("institution", CurrentInstitution.get());
                for (Object b : q.list()) {
                  session.delete(b);
                }
                return null;
              }
            });
  }

  @Override
  public void changeOwnership(final String fromUser, final String toUser) {
    getHibernateTemplate()
        .execute(
            new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) {
                Query q =
                    session.createQuery(
                        "UPDATE Bookmark SET owner = :toUser WHERE owner = :fromUser"
                            + " AND institution = :institution");
                q.setParameter("fromUser", fromUser);
                q.setParameter("toUser", toUser);
                q.setParameter("institution", CurrentInstitution.get());
                q.executeUpdate();
                return null;
              }
            });
  }

  @Override
  public Collection<Bookmark> getAllMentioningItem(Item item) {
    return (Collection<Bookmark>)
        getHibernateTemplate().find("FROM Bookmark WHERE item = ?0", new Object[] {item});
  }

  @Override
  public List<Item> updateAlwaysLatest(Item newItem) {
    List<Item> reindex = new ArrayList<Item>();

    // This could probably be done in a single bulk update query
    List<Bookmark> bookmarks =
        (List<Bookmark>)
            getHibernateTemplate()
                .find(
                    "FROM Bookmark WHERE institution = ?0 AND item.uuid = ?1 AND always_latest ="
                        + " true AND item <> ?2",
                    new Object[] {CurrentInstitution.get(), newItem.getUuid(), newItem});
    for (Bookmark b : bookmarks) {
      Item existingItem = b.getItem();

      reindex.add(existingItem);
      b.setItem(newItem);
      save(b);
    }

    return reindex;
  }

  @Override
  public List<Item> filterNonBookmarkedItems(final Collection<Item> items) {
    if (items.isEmpty()) {
      return Collections.emptyList();
    }
    return (List<Item>)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) {
                    Query q =
                        session.createQuery(
                            "SELECT b.item FROM Bookmark b WHERE b.owner = :owner"
                                + " AND b.institution = :institution AND b.item IN (:items)");
                    q.setParameter("owner", CurrentUser.getUserID());
                    q.setParameter("institution", CurrentInstitution.get());
                    q.setParameterList("items", items);
                    return q.list();
                  }
                });
  }

  @Override
  public Map<Long, List<Bookmark>> getBookmarksForIds(final Collection<Long> ids) {
    Map<Long, List<Bookmark>> map = new HashMap<Long, List<Bookmark>>();
    if (ids.isEmpty()) {
      return map;
    }

    List<Bookmark> bookmarks =
        (List<Bookmark>)
            getHibernateTemplate()
                .findByNamedParam(
                    "FROM Bookmark b LEFT JOIN FETCH b.keywords WHERE b.item.id IN (:ids)",
                    "ids",
                    ids);

    for (Bookmark b : bookmarks) {
      long id = b.getItem().getId();

      List<Bookmark> list = map.get(id);
      if (list == null) {
        list = new ArrayList<Bookmark>();
        map.put(id, list);
      }
      list.add(b);
    }
    return map;
  }

  @Override
  public void delete(Item item) {
    Collection<Bookmark> bookmarks = getAllMentioningItem(item);
    for (Bookmark bookmark : bookmarks) {
      delete(bookmark);
    }
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items, String userId) {
    if (items.isEmpty()) {
      return Collections.emptyMap();
    }

    final List<Bookmark> bs =
        (List<Bookmark>)
            getHibernateTemplate()
                .findByNamedParam(
                    "FROM Bookmark b WHERE b.item IN (:items) and b.owner = :ownerId",
                    new String[] {"items", "ownerId"},
                    new Object[] {items, userId});

    final Map<Item, Bookmark> rv = new HashMap<Item, Bookmark>();
    for (Bookmark b : bs) {
      rv.put(b.getItem(), b);
    }
    return rv;
  }
}
