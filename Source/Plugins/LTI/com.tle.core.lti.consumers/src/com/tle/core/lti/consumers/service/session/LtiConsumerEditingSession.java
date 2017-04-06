package com.tle.core.lti.consumers.service.session;

import com.tle.common.EntityPack;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class LtiConsumerEditingSession extends EntityEditingSessionImpl<LtiConsumerEditingBean, LtiConsumer>
{

	public LtiConsumerEditingSession(String sessionId, EntityPack<LtiConsumer> pack, LtiConsumerEditingBean bean)
	{
		super(sessionId, pack, bean);
	}

}
