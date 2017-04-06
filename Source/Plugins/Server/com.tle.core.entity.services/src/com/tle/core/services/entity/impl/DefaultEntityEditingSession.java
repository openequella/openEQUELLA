package com.tle.core.services.entity.impl;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.EntityPack;
import com.tle.core.services.entity.EntityEditingBean;

/**
 * @author aholland
 */
public class DefaultEntityEditingSession extends EntityEditingSessionImpl<EntityEditingBean, BaseEntity>
{
	private static final long serialVersionUID = 1L;

	public DefaultEntityEditingSession(String sessionId, EntityPack<BaseEntity> pack, EntityEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
