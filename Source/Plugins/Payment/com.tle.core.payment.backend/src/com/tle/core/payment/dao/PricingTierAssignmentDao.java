package com.tle.core.payment.dao;

import java.util.List;
import java.util.Map;

import com.tle.beans.item.Item;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author aholland
 */
public interface PricingTierAssignmentDao extends GenericDao<PricingTierAssignment, Long>
{
	PricingTierAssignment getForItem(Item item);

	List<PricingTierAssignment> enumerateForTier(PricingTier tier);

	List<PricingTierAssignment> enumerateAll();

	Map<Item, PricingTierAssignment> getAssignmentsForItems(List<Item> items);
}
