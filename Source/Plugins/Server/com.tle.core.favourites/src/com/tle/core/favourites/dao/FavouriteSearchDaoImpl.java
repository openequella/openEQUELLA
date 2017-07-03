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

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.common.usermanagement.user.CurrentUser;

@Bind(FavouriteSearchDao.class)
@Singleton
public class FavouriteSearchDaoImpl extends GenericInstitionalDaoImpl<FavouriteSearch, Long>
	implements
		FavouriteSearchDao
{

	public FavouriteSearchDaoImpl()
	{
		super(FavouriteSearch.class);
	}

	@Override
	public List<FavouriteSearch> search(String freetext, Date[] dates, int offset, int perPage, String orderby,
		boolean reverse, String userId, Institution institution)
	{
		return enumerateAll(
			new FavouriteSearchListCallback(freetext, dates, offset, perPage, orderby, reverse, userId, institution));
	}

	@SuppressWarnings({"nls", "unchecked"})
	@Override
	public FavouriteSearch getById(long id)
	{
		List<FavouriteSearch> search = getHibernateTemplate().findByNamedParam(
			"from FavouriteSearch where id = :id and owner = :owner and institution = :inst",
			new String[]{"id", "owner", "inst"}, new Object[]{id, CurrentUser.getUserID(), CurrentInstitution.get()});
		return uniqueResult(search);
	}

	@SuppressWarnings("nls")
	protected static class FavouriteSearchListCallback implements ListCallback
	{
		private final String freetext;
		private final int offset;
		private final int max;
		private final String orderby;
		private final boolean reverse;
		private final Date[] dates;
		private final String userId;
		private final Institution institution;

		public FavouriteSearchListCallback(String freetext, Date[] dates, int offset, int max, String orderby,
			boolean reverse, String userId, Institution institution)
		{
			this.freetext = (Check.isEmpty(freetext) ? null : '%' + freetext.trim().toLowerCase() + '%');
			this.offset = offset;
			this.max = max;
			this.orderby = orderby;
			this.reverse = reverse;
			this.dates = dates;
			this.userId = userId;
			this.institution = institution;
		}

		@Override
		public String getAdditionalJoins()
		{
			return null;
		}

		private void addClause(StringBuilder query, String clause)
		{
			if( query.length() > 0 )
			{
				query.append(" AND ");
			}
			query.append(clause);
		}

		@Override
		public String getAdditionalWhere()
		{
			StringBuilder additional = new StringBuilder();
			if( freetext != null )
			{
				addClause(additional, "be.name LIKE :freetext");
			}
			if( dates != null )
			{
				Date start = dates[0];
				Date end = dates[1];

				if( start != null && end != null ) // BETWEEN and ON
				{
					addClause(additional, "be.dateModified BETWEEN :start AND :end");
				}
				else if( start != null && end == null ) // AFTER
				{
					addClause(additional, "be.dateModified >= :start");
				}
				else if( start == null && end != null ) // BEFORE
				{
					addClause(additional, "be.dateModified <= :end");
				}
			}
			addClause(additional, "be.owner = :owner");
			return additional.toString();
		}

		@Override
		public boolean isDistinct()
		{
			return false;
		}

		@Override
		public String getOrderBy()
		{
			if( orderby != null )
			{
				String orderByString = " ORDER BY " + orderby; //$NON-NLS-1$
				if( !reverse )
				{
					orderByString += " DESC"; //$NON-NLS-1$
				}
				else
				{
					orderByString += " ASC"; //$NON-NLS-1$
				}
				return orderByString;
			}
			return null;
		}

		@Override
		public void processQuery(Query query)
		{
			if( freetext != null )
			{
				query.setParameter("freetext", freetext); //$NON-NLS-1$
			}
			if( dates != null )
			{
				if( dates[0] != null )
				{
					query.setParameter("start", dates[0]); //$NON-NLS-1$
				}
				if( dates[1] != null )
				{
					query.setParameter("end", dates[1]); //$NON-NLS-1$
				}
			}
			query.setParameter("owner", userId);
			query.setParameter("institution", institution);

			query.setFirstResult(offset);
			query.setFetchSize(max);
			query.setMaxResults(max);
		}

	}

	@Override
	public void deleteAll()
	{
		List<FavouriteSearch> favSearches = enumerateAll();
		for( FavouriteSearch fs : favSearches )
		{
			delete(fs);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public long count(String freetext, Date[] dates, String userId, Institution institution)
	{
		Criterion dateRestriction = null;
		if( dates != null )
		{
			Date start = dates[0];
			Date end = dates[1];

			if( start != null && end != null ) // BETWEEN and ON
			{
				dateRestriction = Restrictions.between("dateModified", start, end);
			}
			else if( start != null && end == null ) // AFTER
			{
				dateRestriction = Restrictions.ge("dateModified", start);
			}
			else if( start == null && end != null ) // BEFORE
			{
				dateRestriction = Restrictions.le("dateModified", end);
			}
		}
		if( dateRestriction != null )
		{
			return countByCriteria(Restrictions.eq("owner", userId), Restrictions.eq("institution", institution),
				dateRestriction, Restrictions.ilike("name", "%" + freetext + "%"));
		}
		else
		{
			return countByCriteria(Restrictions.eq("owner", userId), Restrictions.eq("institution", institution),
				Restrictions.ilike("name", "%" + freetext + "%"));
		}

	}
}
