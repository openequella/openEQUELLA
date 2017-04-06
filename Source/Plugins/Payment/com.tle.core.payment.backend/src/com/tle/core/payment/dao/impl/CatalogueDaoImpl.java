package com.tle.core.payment.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.entity.DynaCollection;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Region;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.CatalogueDao;

@Bind(CatalogueDao.class)
@Singleton
@SuppressWarnings("nls")
public class CatalogueDaoImpl extends AbstractEntityDaoImpl<Catalogue> implements CatalogueDao
{
	public CatalogueDaoImpl()
	{
		super(Catalogue.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isExistingReferences(DynaCollection dynaCollection)
	{
		return ((List<Number>) getHibernateTemplate().find(
			"SELECT COUNT(*) FROM Catalogue WHERE dynamicCollection = ?", dynaCollection)).get(0).longValue() != 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Catalogue> enumerateByRegion(final Region region, final boolean enabledOnly)
	{
		return (List<Catalogue>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				String query = "FROM Catalogue WHERE :region IN elements(regions)";
				if( enabledOnly )
				{
					query += " AND disabled = :disabled";
				}
				final Query q = session.createQuery(query).setParameter("region", region);
				if( enabledOnly )
				{
					q.setParameter("disabled", false);
				}
				return q.list();
			}
		});
	}
}
