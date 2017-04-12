package com.tle.core.payment.service;

import java.util.List;
import java.util.Map;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.ListMultimap;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.payment.service.session.PricingTierEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

public interface PricingTierService extends AbstractEntityService<PricingTierEditingBean, PricingTier>
{
	List<BaseEntityLabel> listAll(boolean purchase);

	List<BaseEntityLabel> listEnabled(boolean purchase);

	List<BaseEntityLabel> listEditable(boolean purchase);

	List<PricingTier> enumerateAll(boolean purchase);

	List<PricingTier> enumerateEnabled(boolean purchase);

	List<PricingTier> enumerateEditable(boolean purchase);

	List<PricingTierAssignment> listAssignmentsForTier(PricingTier tier);

	Map<Item, PricingTierAssignment> getAssignmentsForItems(List<Item> items);

	Long createPricingTierAssignment(ItemKey itemKey, PricingTier purchaseTier, PricingTier subscriptionTier,
		boolean free);

	PricingTierAssignment getPricingTierAssignmentForItem(ItemKey itemKey);

	Long savePricingTierAssignment(PricingTierAssignment pricingTierAssignment);

	/**
	 * @param tier Must be a purchase tier
	 * @return
	 */
	Price getPriceForPurchaseTier(PricingTier tier);

	/**
	 * @param tier Must be a subscription tier
	 * @return
	 */
	Price getPriceForSubscriptionTierAndPeriod(PricingTier tier, SubscriptionPeriod period);

	Map<SubscriptionPeriod, Price> getPriceMapForSubscriptionTier(PricingTier tier);

	/**
	 * Ordered by subscription period 'magnitude'
	 * 
	 * @param tier Must be a subscription tier
	 * @return
	 */
	List<Price> enumeratePricesForSubscriptionTier(PricingTier tier);

	/**
	 * @param purchase
	 * @return Enabled tiers
	 */
	ListMultimap<PricingTier, Price> getEnabledTierPriceMap(boolean purchase);

	FreeTextQuery getPriceSetQuery(boolean isSet);

	FreeTextQuery getPriceSetQuery(boolean isSet, StoreFront storefront);
}
