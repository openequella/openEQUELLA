package com.tle.core.payment.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.PricingTierDao;
import com.tle.core.user.CurrentInstitution;

@Bind(PricingTierDao.class)
@Singleton
@SuppressWarnings({"nls", "unchecked"})
public class PricingTierDaoImpl extends AbstractEntityDaoImpl<PricingTier> implements PricingTierDao
{
	public PricingTierDaoImpl()
	{
		super(PricingTier.class);
	}

	@Override
	public List<BaseEntityLabel> listAll(boolean purchase)
	{
		return listAll(PricingTier.ENTITY_TYPE, new TierTypeListCallback(purchase, null));
	}

	@Override
	public List<BaseEntityLabel> listEnabled(boolean purchase)
	{
		return listAll(PricingTier.ENTITY_TYPE, new TierTypeListCallback(purchase, true));
	}

	@Override
	public List<PricingTier> enumerateAll(boolean purchase)
	{
		return enumerateAll(new TierTypeListCallback(purchase, null));
	}

	@Override
	public List<PricingTier> enumerateEnabled(boolean purchase)
	{
		return enumerateAll(new TierTypeListCallback(purchase, true));
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();

		// Ugh
		if( ((List<Long>) getHibernateTemplate().findByNamedParam(
			"select count(*) from PricingTierAssignment pta where pta.purchasePricingTier.id = :id", "id", id)).get(0) != 0 )
		{
			classes.add(PricingTierAssignment.class);
		}
		if( ((List<Long>) getHibernateTemplate().findByNamedParam(
			"select count(*) from PricingTierAssignment pta where pta.subscriptionPricingTier.id = :id", "id", id))
			.get(0) != 0 )
		{
			if( !classes.contains(PricingTierAssignment.class) )
			{
				classes.add(PricingTierAssignment.class);
			}
		}
		return classes;
	}

	@Override
	public ListMultimap<PricingTier, Price> getEnabledTierPriceMap(boolean purchase)
	{
		List<Price> prices = getHibernateTemplate()
			.findByNamedParam(
				"select p from Price p where p.tier.institution = :institution and p.tier.purchase = :purchase and p.tier.disabled = false order by p.value ASC",
				new String[]{"purchase", "institution"}, new Object[]{purchase, CurrentInstitution.get()});

		ListMultimap<PricingTier, Price> multiMap = ArrayListMultimap.create();
		for( Price price : prices )
		{
			multiMap.put(price.getTier(), price);
		}
		return multiMap;
	}

	private static class TierTypeListCallback extends EnabledCallback
	{
		private final boolean purchase;

		/**
		 * @param purchase
		 * @param enabled Tri-state value: true = enabled only, false = disabled
		 *            only, null = no filter
		 */
		public TierTypeListCallback(boolean purchase, Boolean enabled)
		{
			super(enabled);
			this.purchase = purchase;
		}

		@Override
		public String getAdditionalWhere()
		{
			String additionalWhere = super.getAdditionalWhere();
			if( !Check.isEmpty(additionalWhere) )
			{
				additionalWhere += " AND ";
			}
			return additionalWhere + " be.purchase = :purchase";
		}

		@Override
		public void processQuery(Query query)
		{
			super.processQuery(query);
			query.setParameter("purchase", purchase);
		}
	}
}
