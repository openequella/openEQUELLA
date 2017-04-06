package com.tle.core.payment.dao;

import java.util.List;
import java.util.Map;

import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author aholland
 */
public interface PriceDao extends GenericDao<Price, Long>
{
	List<Price> enumerateAllForSubscriptionTier(PricingTier tier);

	Price getForPurchaseTier(PricingTier tier);

	Price getForSubscriptionTierAndPeriod(PricingTier tier, SubscriptionPeriod period);

	Map<SubscriptionPeriod, Price> getPriceMapForSubscriptionTier(PricingTier tier);

	List<Price> enumerateByCatalogueAndRegion(Catalogue catalogue, Region region);

	List<Price> enumerateByRegion(Region region);

	List<Price> enumerateByCatalogue(Catalogue catalogue);

	List<Price> enumerateForInstitution();

	Map<Long, Price> findByIds(List<Long> ids);
}
