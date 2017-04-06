package com.tle.core.payment.events;

import com.tle.common.payment.entity.PricingTier;
import com.tle.core.events.BaseEntityDeletionEvent;
import com.tle.core.payment.events.listeners.PricingTierDeletionListener;

/**
 * @author Aaron
 */
public class PricingTierDeletionEvent extends BaseEntityDeletionEvent<PricingTier, PricingTierDeletionListener>
{
	private static final long serialVersionUID = 1L;

	public PricingTierDeletionEvent(PricingTier tier)
	{
		super(tier);
	}

	@Override
	public Class<PricingTierDeletionListener> getListener()
	{
		return PricingTierDeletionListener.class;
	}

	@Override
	public void postEvent(PricingTierDeletionListener listener)
	{
		listener.removePricingTierReferences(entity);
	}

}
