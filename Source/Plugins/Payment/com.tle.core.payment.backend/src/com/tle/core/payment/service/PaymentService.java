package com.tle.core.payment.service;

import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.common.payment.entity.SubscriptionPeriod;

public interface PaymentService
{
	/**
	 * Ordered by subscription period 'magnitude'
	 * 
	 * @return
	 */
	List<SubscriptionPeriod> enumerateSubscriptionPeriods();

	/**
	 * @param uuid
	 * @return
	 */
	SubscriptionPeriod getSubscriptionPeriodByUuid(String uuid);

	/**
	 * @return An unordered list of currencies
	 */
	Set<Currency> getCurrencies();

	/**
	 * Updates all Prices for the specified catalogue and region (currently both
	 * MUST be null) to use the supplied currency
	 * 
	 * @param region
	 * @param currency
	 */
	void changeCurrency(Catalogue catalogue, Region region, Currency currency);

	/**
	 * Utility converter method: for a given start date, what is the end date of
	 * a specified SubscriptionPeriod?
	 * 
	 * @param startDate Date commencement of period
	 * @param subscriptionPeriod
	 */
	Date getEndDateOfSubscriptionPeriod(Date startDate, SubscriptionPeriod subscriptionPeriod);

	void recordHarvest(String itemUuid, int version, Sale sale);

	void recordHarvest(String itemUuid, int version, String attachUuid, Sale sale);

	List<StoreHarvestInfo> getHarvestHistoryForSaleItem(String saleItemUuid);
}
