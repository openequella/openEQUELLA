package com.tle.core.payment.storefront.service.session;

import com.tle.common.EntityPack;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class StoreEditingSession extends EntityEditingSessionImpl<StoreEditingBean, Store>
{
	private static final long serialVersionUID = 1L;

	public StoreEditingSession(String sessionId, EntityPack<Store> pack, StoreEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
