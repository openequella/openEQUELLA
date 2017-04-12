package com.tle.core.payment.dao;

import java.util.Date;
import java.util.List;

import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author aholland
 */
public interface SaleItemDao extends GenericDao<SaleItem, Long>
{
	/**
	 * An unrefined search for items which are the subject of a SaleItem. In
	 * practise, it would be expected that returned items, where the saleItem
	 * was for a subscription, be examined to see if their subscription period
	 * falls within a date range, for example, now current?
	 * 
	 * @param storeFront
	 * @param uuid
	 * @param mustBePaidFor
	 * @return SaleItem list
	 */
	List<SaleItem> findSaleItemByItemUuid(final StoreFront storeFront, final String uuid, final boolean mustBePaidFor);

	/**
	 * Retrieving paid for SaleItems from a store institution, paid for by a
	 * store front, where paid for is established either by being free, or<br>
	 * a paid-for purchase, or<br>
	 * a paid for subscription which has a start date of now or in the past and
	 * an end date now or in the future.<br>
	 * 
	 * @param storeFront
	 * @param referenceDate caller's reference point (today for example)
	 * @param onlyNewFlag Narrows the list to items that have never been
	 *            downloaded
	 * @param createdSince Narrows the list to bought items that have been
	 *            created since this date
	 * @param offset result set start
	 * @param max result set maximum size
	 */
	List<SaleItem> getPaidForSaleItems(final StoreFront storeFront, final Date referenceDate,
		final boolean onlyNewFlag, final Date createdSince, final int offset, final int max);

	/**
	 * When the caller passes 'true' for onlyNewFlag, we take it that the mere
	 * presence of a StoreHarvestInfo which matches the SaleItem's Sale and the
	 * SaleItem's itemUuid means we can assume that the purchase has been
	 * retrieved at some past point by the buyer
	 */
	int countPaidForSaleItems(final StoreFront storeFront, final Date referenceDate, final boolean onlyNewFlag,
		final Date createdSince);

	List<SaleItem> getSubscriptionsActiveWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod, final int offset, final int max);

	int countSubscriptionsActiveWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod);

	List<SaleItem> getSubscriptionsExpiringWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod, final int offset, final int max);

	int countSubscriptionsExpiringWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod);

	List<SaleItem> getSalesItemsForSourceItem(String itemUuid);
}
