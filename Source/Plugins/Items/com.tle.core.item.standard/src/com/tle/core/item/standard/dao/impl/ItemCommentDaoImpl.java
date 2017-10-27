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

package com.tle.core.item.standard.dao.impl;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.tle.beans.item.Comment;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.item.standard.dao.ItemCommentDao;
import com.tle.core.item.standard.service.ItemCommentService.CommentFilter;
import com.tle.core.item.standard.service.ItemCommentService.CommentOrder;

/**
 * @author Nicholas Read
 */
@Bind(ItemCommentDao.class)
@Singleton
@SuppressWarnings("nls")
public class ItemCommentDaoImpl extends GenericDaoImpl<Comment, Long> implements ItemCommentDao
{
	public ItemCommentDaoImpl()
	{
		super(Comment.class);
	}

	@Override
	public float getAverageRatingForItem(final ItemKey itemId)
	{
		return (Float) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Object res = session.getNamedQuery("getAverageRatingForItem").setParameter("uuid", itemId.getUuid())
						.setParameter("version", itemId.getVersion()).setParameter("institution", CurrentInstitution.get())
						.uniqueResult();
				if (res == null)
				{
					res = 0F;
				}
				return res;
			}
		});
	}

	@Override
	public Comment getByUuid(final Item item, final String uuid)
	{
		return (Comment) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.getNamedQuery("getItemCommentByUuid").setParameter("uuid", uuid)
					.setParameter("item", item).uniqueResult();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Comment> getComments(final Item item, final EnumSet<CommentFilter> filter, final CommentOrder order,
		final int limit)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				// SQL QUERY EXPLANATION!
				//
				// The following is a native SQL query, but Hibernate still
				// gives us some love to avoid nastiness. It needs to be a
				// native SQL query because HQL doesn't allow for joining onto a
				// selected set of result, eg, ...FROM (SELECT ...)...
				//
				// Tread very carefully.

				StringBuilder sb = new StringBuilder("SELECT {c.*} FROM comments c");

				if( filter.contains(CommentFilter.ONLY_MOST_RECENT_PER_USER) )
				{
					sb.append(" JOIN (");
					sb.append("   SELECT owner, max(date_created) as date_created");
					sb.append("   FROM comments");
					sb.append("   WHERE item_id = :itemdbid");
					sb.append("   AND owner IS NOT NULL");

					if( filter.contains(CommentFilter.NOT_ANONYMOUS_OR_GUEST) )
					{
						// We already filter out blank guest rows when we do the
						// group by, so only filter out anonymous comments here.
						sb.append("   AND anonymous = :false");
					}

					if( filter.contains(CommentFilter.MUST_HAVE_COMMENT) )
					{
						sb.append(" AND \"comment\" IS NOT NULL");
					}

					if( filter.contains(CommentFilter.MUST_HAVE_RATING) )
					{
						sb.append(" AND rating > 0");
					}

					sb.append("   GROUP BY owner");
					sb.append(" ) lc ON (c.owner = lc.owner AND c.date_created = lc.date_created)");

					if( !filter.contains(CommentFilter.NOT_ANONYMOUS_OR_GUEST) )
					{
						// Include all guest comments back into the results if
						// allowed - notice the negation in the IF statement!
						sb.append(" OR c.owner IS NULL");
					}
				}

				sb.append(" WHERE c.item_id = :itemdbid");

				// We only need to apply the following if we're **NOT**
				// filtering by ONLY_MOST_RECENT_PER_USER, as the sub-query
				// already filters all these out in that case.
				if( !filter.contains(CommentFilter.ONLY_MOST_RECENT_PER_USER) )
				{
					if( filter.contains(CommentFilter.NOT_ANONYMOUS_OR_GUEST) )
					{
						sb.append(" AND c.owner IS NOT NULL AND c.anonymous = :false");
					}

					if( filter.contains(CommentFilter.MUST_HAVE_COMMENT) )
					{
						sb.append(" AND c.\"comment\" IS NOT NULL");
					}

					if( filter.contains(CommentFilter.MUST_HAVE_RATING) )
					{
						sb.append(" AND c.rating > 0");
					}
				}

				sb.append(" ORDER BY ");
				switch( order )
				{
					case REVERSE_CHRONOLOGICAL:
						sb.append("c.date_created DESC");
						break;
					case CHRONOLOGICAL:
						sb.append("c.date_created ASC");
						break;
					case HIGHEST_RATED:
						sb.append("c.rating DESC");
						break;
					case LOWEST_RATED:
						sb.append("c.rating ASC");
						break;
				}

				SQLQuery q = session.createSQLQuery(sb.toString());
				q.addEntity("c", Comment.class);
				q.setCacheable(true);
				q.setReadOnly(true);
				// We want to use the item DB ID directly because the Item
				// object may not be a serialised, e.g. during a contribution
				// preview, which Hibernate complains about when binding the
				// parameter.
				q.setParameter("itemdbid", item.getId());
				if( filter.contains(CommentFilter.NOT_ANONYMOUS_OR_GUEST) )
				{
					q.setBoolean("false", false);
				}
				if( limit > 0 )
				{
					q.setMaxResults(limit);
				}
				return q.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Multimap<Long, Comment> getCommentsForItems(Collection<Long> itemIds)
	{
		if( itemIds.isEmpty() )
		{
			return ImmutableListMultimap.of();
		}
		List<Object[]> attachments = getHibernateTemplate()
			.findByNamedParam("select c, i.id from Item i join i.comments c where i.id in (:items)", "items", itemIds);
		ListMultimap<Long, Comment> multiMap = ArrayListMultimap.create();
		for( Object[] attachmentRow : attachments )
		{
			multiMap.put((Long) attachmentRow[1], (Comment) attachmentRow[0]);
		}
		return multiMap;
	}

	@Override
	public Collection<ItemIdKey> getItemKeysForUserComments(String userId)
	{
		return getHibernateTemplate().findByNamedParam(
			"SELECT new com.tle.beans.item.ItemIdKey(i.id, i.uuid, i.version) FROM Comment c"
				+ " JOIN c.item i WHERE c.owner = :owner AND i.institution = :institution",
			new String[]{"owner", "institution"}, new Object[]{userId, CurrentInstitution.get()});
	}
}
