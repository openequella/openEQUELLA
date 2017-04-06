package com.tle.core.payment.dao;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.core.dao.AbstractEntityDao;

public interface PricingTierDao extends AbstractEntityDao<PricingTier>
{
	List<BaseEntityLabel> listAll(boolean purchase);

	List<BaseEntityLabel> listEnabled(boolean purchase);

	List<PricingTier> enumerateAll(boolean purchase);

	List<PricingTier> enumerateEnabled(boolean purchase);

	ListMultimap<PricingTier, Price> getEnabledTierPriceMap(boolean purchase);

	List<Class<?>> getReferencingClasses(long id);
}
