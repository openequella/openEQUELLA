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

package com.tle.core.favourites.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.item.dao.ItemDaoExtension;
import com.tle.common.usermanagement.user.CurrentUser;

@SuppressWarnings("nls")
@Bind(BookmarkDao.class)
@Singleton
public class BookmarkDaoImpl extends GenericInstitionalDaoImpl<Bookmark, Long> implements BookmarkDao, ItemDaoExtension
{
	public BookmarkDaoImpl()
	{
		super(Bookmark.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Bookmark findById(long id)
	{
		List<Bookmark> list = getHibernateTemplate().find("FROM Bookmark WHERE id = ? AND institution = ?",
			new Object[]{id, CurrentInstitution.get()});
		return list.isEmpty() ? null : list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bookmark getByItemAndUserId(String userId, ItemKey itemId)
	{
		List<Bookmark> find = getHibernateTemplate().find(
			"FROM Bookmark WHERE owner = ? AND item.uuid = ? AND item.version = ? AND institution = ?",
			new Object[]{userId, itemId.getUuid(), itemId.getVersion(), CurrentInstitution.get()});
		return find.isEmpty() ? null : find.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Bookmark> listAll()
	{
		return getHibernateTemplate().find("FROM Bookmark WHERE institution = ?",
			new Object[]{CurrentInstitution.get()});
	}

	@Override
	public void deleteAll()
	{
		for( Bookmark bookmark : listAll() )
		{
			delete(bookmark);
		}
	}

	@Override
	public void deleteAllForUser(final String user)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("FROM Bookmark WHERE owner = :owner AND institution = :institution");
				q.setParameter("owner", user);
				q.setParameter("institution", CurrentInstitution.get());
				for( Object b : q.list() )
				{
					session.delete(b);
				}
				return null;
			}
		});
	}

	@Override
	public void changeOwnership(final String fromUser, final String toUser)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery(
					"UPDATE Bookmark SET owner = :toUser WHERE owner = :fromUser" + " AND institution = :institution");
				q.setParameter("fromUser", fromUser);
				q.setParameter("toUser", toUser);
				q.setParameter("institution", CurrentInstitution.get());
				q.executeUpdate();
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Bookmark> getAllMentioningItem(Item item)
	{
		return getHibernateTemplate().find("FROM Bookmark WHERE item = ?", new Object[]{item});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Item> updateAlwaysLatest(Item newItem)
	{
		List<Item> reindex = new ArrayList<Item>();

		// This could probably be done in a single bulk update query
		List<Bookmark> bookmarks = getHibernateTemplate().find(
			"FROM Bookmark WHERE institution = ? AND item.uuid = ? AND always_latest = true AND item <> ?",
			new Object[]{CurrentInstitution.get(), newItem.getUuid(), newItem});
		for( Bookmark b : bookmarks )
		{
			Item existingItem = b.getItem();

			reindex.add(existingItem);
			b.setItem(newItem);
			save(b);
		}

		return reindex;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public List<Item> filterNonBookmarkedItems(final Collection<Item> items)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("SELECT b.item FROM Bookmark b WHERE b.owner = :owner"
					+ " AND b.institution = :institution AND b.item IN (:items)");
				q.setParameter("owner", CurrentUser.getUserID());
				q.setParameter("institution", CurrentInstitution.get());
				q.setParameterList("items", items);
				return q.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, List<Bookmark>> getBookmarksForIds(final Collection<Long> ids)
	{
		Map<Long, List<Bookmark>> map = new HashMap<Long, List<Bookmark>>();
		if( ids.isEmpty() )
		{
			return map;
		}

		List<Bookmark> bookmarks = getHibernateTemplate()
			.findByNamedParam("FROM Bookmark b LEFT JOIN FETCH b.keywords WHERE b.item.id IN (:ids)", "ids", ids);

		for( Bookmark b : bookmarks )
		{
			long id = b.getItem().getId();

			List<Bookmark> list = map.get(id);
			if( list == null )
			{
				list = new ArrayList<Bookmark>();
				map.put(id, list);
			}
			list.add(b);
		}
		return map;
	}

	@Override
	public void delete(Item item)
	{
		Collection<Bookmark> bookmarks = getAllMentioningItem(item);
		for( Bookmark bookmark : bookmarks )
		{
			delete(bookmark);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items, String userId)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyMap();
		}

		final List<Bookmark> bs = getHibernateTemplate().findByNamedParam(
			"FROM Bookmark b WHERE b.item IN (:items) and b.owner = :ownerId", new String[]{"items", "ownerId"},
			new Object[]{items, userId});

		final Map<Item, Bookmark> rv = new HashMap<Item, Bookmark>();
		for( Bookmark b : bs )
		{
			rv.put(b.getItem(), b);
		}
		return rv;
	}
}
