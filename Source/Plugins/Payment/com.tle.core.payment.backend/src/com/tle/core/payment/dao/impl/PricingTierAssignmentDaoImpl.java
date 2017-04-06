package com.tle.core.payment.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.dao.ItemDaoExtension;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.payment.dao.PricingTierAssignmentDao;
import com.tle.core.user.CurrentInstitution;

@SuppressWarnings("nls")
@Bind(PricingTierAssignmentDao.class)
@Singleton
public class PricingTierAssignmentDaoImpl extends GenericDaoImpl<PricingTierAssignment, Long>
	implements
		PricingTierAssignmentDao,
		ItemDaoExtension
{
	public PricingTierAssignmentDaoImpl()
	{
		super(PricingTierAssignment.class);
	}

	@Override
	public PricingTierAssignment getForItem(Item item)
	{
		return uniqueResult(findAllByCriteria(Restrictions.eq("item", item)));
	}

	// ItemDaoExtension
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void delete(Item item)
	{
		getHibernateTemplate().bulkUpdate("DELETE FROM PricingTierAssignment WHERE item = ?", item);
	}

	@Override
	public List<PricingTierAssignment> enumerateForTier(PricingTier tier)
	{
		return findAllByCriteria(Restrictions.eq(tier.isPurchase() ? "purchasePricingTier" : "subscriptionPricingTier",
			tier));
	}

	@Override
	public List<PricingTierAssignment> enumerateAll()
	{
		DetachedCriteria dc = DetachedCriteria.forClass(PricingTierAssignment.class);
		dc.createCriteria("item").add(Restrictions.eq("institution", CurrentInstitution.get()));

		return findAnyByCriteria(dc, null, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Item, PricingTierAssignment> getAssignmentsForItems(final List<Item> items)
	{
		final List<PricingTierAssignment> ptas = getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				if( Check.isEmpty(items) )
				{
					return new ArrayList<PricingTierAssignment>();
				}
				Query query = session.createQuery("FROM PricingTierAssignment pta WHERE pta.item in (:items)")
					.setParameterList("items", items);
				return query.list();
			}
		});
		return Maps.uniqueIndex(ptas, new Function<PricingTierAssignment, Item>()
		{
			@Override
			public Item apply(PricingTierAssignment input)
			{
				return input.getItem();
			}
		});
	}
}
