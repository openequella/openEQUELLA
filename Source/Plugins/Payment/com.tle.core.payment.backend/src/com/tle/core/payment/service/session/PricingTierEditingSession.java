package com.tle.core.payment.service.session;

import com.tle.common.EntityPack;
import com.tle.common.payment.entity.PricingTier;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class PricingTierEditingSession extends EntityEditingSessionImpl<PricingTierEditingBean, PricingTier>
{
	private static final long serialVersionUID = 1L;

	public PricingTierEditingSession(String sessionId, EntityPack<PricingTier> pack, PricingTierEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
