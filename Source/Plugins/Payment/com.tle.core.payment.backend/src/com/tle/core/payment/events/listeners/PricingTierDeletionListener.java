package com.tle.core.payment.events.listeners;

import com.tle.common.payment.entity.PricingTier;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface PricingTierDeletionListener extends ApplicationListener
{
	void removePricingTierReferences(PricingTier tier);
}
