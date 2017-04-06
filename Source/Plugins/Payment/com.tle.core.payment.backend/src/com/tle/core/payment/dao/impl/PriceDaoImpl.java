package com.tle.core.payment.dao.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.google.common.collect.Maps;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.payment.dao.PriceDao;
import com.tle.core.user.CurrentInstitution;

@Bind(PriceDao.class)
@Singleton
@SuppressWarnings("nls")
public class PriceDaoImpl extends GenericDaoImpl<Price, Long> implements PriceDao
{
	public PriceDaoImpl()
	{
		super(Price.class);
	}

	@Override
	public List<Price> enumerateAllForSubscriptionTier(final PricingTier tier)
	{
		DetachedCriteria crit = DetachedCriteria.forClass(Price.class, "p").createAlias("period", "pd")
			.addOrder(Order.asc("pd.magnitude")).add(Restrictions.eq("p.tier", tier));
		return findAnyByCriteria(crit, null, null);
	}

	@Override
	public Price getForPurchaseTier(PricingTier tier)
	{
		return findByCriteria(Restrictions.eq("tier", tier));
	}

	@Override
	public Price getForSubscriptionTierAndPeriod(PricingTier tier, SubscriptionPeriod period)
	{
		return findByCriteria(Restrictions.eq("tier", tier), Restrictions.eq("period", period));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<SubscriptionPeriod, Price> getPriceMapForSubscriptionTier(final PricingTier tier)
	{
		final Map<SubscriptionPeriod, Price> map = Maps.newLinkedHashMap();
		final List<Object[]> res = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session
					.createQuery(
						"FROM SubscriptionPeriod period, Price price WHERE price.tier = :tier AND price.period = period ORDER BY period.magnitude")
					.setParameter("tier", tier).list();
			}
		});
		for( Object[] r : res )
		{
			map.put((SubscriptionPeriod) r[0], (Price) r[1]);
		}
		return map;
	}

	@Override
	public List<Price> enumerateByCatalogueAndRegion(Catalogue catalogue, Region region)
	{
		return findAllByCriteria(Restrictions.eq("catalogue", catalogue), Restrictions.eq("region", region));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Price> enumerateByRegion(final Region region)
	{
		return (List<Price>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.createQuery("FROM Price WHERE region = :region").setParameter("region", region).list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Price> enumerateByCatalogue(final Catalogue catalogue)
	{
		return (List<Price>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.createQuery("FROM Price WHERE catalogue = :catalogue")
					.setParameter("catalogue", catalogue).list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Price> enumerateForInstitution()
	{
		return (List<Price>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.createQuery("FROM Price WHERE tier.institution = :institution")
					.setParameter("institution", CurrentInstitution.get()).list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, Price> findByIds(final List<Long> ids)
	{
		final Map<Long, Price> map = Maps.newHashMap();
		final List<Object[]> res = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session
					.createQuery(
						"SELECT p.id, p FROM Price p WHERE p.tier.institution = :institution AND p.id IN (:ids)")
					.setParameter("institution", CurrentInstitution.get()).setParameterList("ids", ids).list();
			}
		});
		for( Object[] r : res )
		{
			map.put((Long) r[0], (Price) r[1]);
		}
		return map;
	}
}
