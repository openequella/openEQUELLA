package com.tle.core.payment.service.session;

import com.tle.common.EntityPack;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class StoreFrontEditingSession extends EntityEditingSessionImpl<StoreFrontEditingBean, StoreFront>
{
	private static final long serialVersionUID = 1L;

	public StoreFrontEditingSession(String sessionId, EntityPack<StoreFront> pack, StoreFrontEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
