/*
 * Created on Oct 26, 2005
 */
package com.tle.core.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.entity.LanguageBundle;
import com.tle.core.dao.BaseEntityDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.AbstractHibernateDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@Bind(BaseEntityDao.class)
@Singleton
public class BaseEntityDaoImpl extends AbstractHibernateDao implements BaseEntityDao
{
	@Override
	public LanguageBundle getEntityNameForId(final long id)
	{
		return (LanguageBundle) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("SELECT name FROM BaseEntity WHERE id = :id");
				query.setParameter("id", id);
				query.setCacheable(true);

				Iterator<?> iter = query.iterate();
				if( iter.hasNext() )
				{
					return iter.next();
				}
				else
				{
					return null;
				}
			}
		});
	}

	@Override
	public Map<Long, String> getUuids(Set<Long> ids)
	{
		Map<Long, String> uuids = new HashMap<Long, String>();
		if( !ids.isEmpty() )
		{
			@SuppressWarnings("unchecked")
			List<Object[]> entityList = getHibernateTemplate().findByNamedParam(
				"SELECT id, uuid FROM BaseEntity WHERE id IN (:ids)", "ids", ids);

			for( Object[] o : entityList )
			{
				uuids.put((Long) o[0], (String) o[1]);
			}
		}
		return uuids;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> getIdsFromUuids(Set<String> uuids)
	{
		if( uuids.isEmpty() )
		{
			return Collections.EMPTY_LIST;
		}

		return getHibernateTemplate().findByNamedParam(
			"SELECT id FROM BaseEntity WHERE institution = :institution AND uuid IN (:uuids)",
			new String[]{"institution", "uuids"}, new Object[]{CurrentInstitution.get(), uuids});
	}
}
