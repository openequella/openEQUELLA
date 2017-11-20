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

package com.tle.core.mimetypes.dao;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.mimetypes.MimeTypesSearchResults;
import com.tle.common.institution.CurrentInstitution;

@Bind(MimeEntryDao.class)
@Singleton
public class MimeEntryDaoImpl extends GenericInstitionalDaoImpl<MimeEntry, Long> implements MimeEntryDao
{

	public MimeEntryDaoImpl()
	{
		super(MimeEntry.class);
	}

	@Override
	public MimeTypesSearchResults searchAll(final String mimeType, final int offset, final int length)
	{
		return (MimeTypesSearchResults) getHibernateTemplate().execute(new HibernateCallback()
		{
			@SuppressWarnings({"nls", "unchecked"})
			@Override
			public Object doInHibernate(Session session)
			{
				String mime = '%' + mimeType.replace('*', '%') + '%';
				String queryString = "FROM MimeEntry m left join m.extensions as e WHERE (LOWER(m.type) LIKE :query OR LOWER(e) LIKE :query) AND m.institution = :institution";
				Query query = session.createQuery("select distinct m " + queryString + " ORDER BY m.type");
				// session.getNamedQuery("searchMimeTypes");
				query.setParameter("query", mime);
				query.setParameter("institution", CurrentInstitution.get());
				query.setFirstResult(offset);
				if( length > 0 )
				{
					query.setMaxResults(length);
				}

				Query count = session.createQuery("select count(distinct m) " + queryString);
				// session.getNamedQuery("countMimeTypes");
				count.setParameter("query", mime);
				count.setParameter("institution", CurrentInstitution.get());

				return new MimeTypesSearchResults(query.list(), offset, ((Long) count.uniqueResult()).intValue());
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MimeEntry> getEntriesForExtensions(final Collection<String> extensions)
	{
		return (List<MimeEntry>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@SuppressWarnings("nls")
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session
					.createQuery("select distinct m from MimeEntry m left join m.extensions e where e IN (:extensions) and m.institution = :institution");
				query.setParameterList("extensions", extensions);
				query.setParameter("institution", CurrentInstitution.get());
				return query.list();
			}
		});
	}

	@Override
	public MimeTypesSearchResults searchByMimeType(final String mimeType, final int offset, final int length)
	{
		return (MimeTypesSearchResults) getHibernateTemplate().execute(new HibernateCallback()
		{
			@SuppressWarnings({"nls", "unchecked"})
			@Override
			public Object doInHibernate(Session session)
			{
				String mime = mimeType.replace('*', '%') + '%';
				Query query = session.getNamedQuery("searchMimeTypes");
				query.setParameter("query", mime);
				query.setParameter("institution", CurrentInstitution.get());
				query.setFirstResult(offset);
				query.setMaxResults(length);

				Query count = session.getNamedQuery("countMimeTypes");
				count.setParameter("query", mime);
				count.setParameter("institution", CurrentInstitution.get());

				return new MimeTypesSearchResults(query.list(), offset, ((Long) count.uniqueResult()).intValue());
			}
		});
	}
}
