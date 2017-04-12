package com.tle.core.payment.service.session;

import com.tle.common.EntityPack;
import com.tle.common.payment.entity.Region;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class RegionEditingSession extends EntityEditingSessionImpl<RegionEditingBean, Region>
{
	private static final long serialVersionUID = 1L;

	public RegionEditingSession(String sessionId, EntityPack<Region> pack, RegionEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
