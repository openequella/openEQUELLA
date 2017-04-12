package com.tle.core.service.session;

import com.tle.common.EntityPack;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class ExternalToolEditingSession extends EntityEditingSessionImpl<ExternalToolEditingBean, ExternalTool>
{

	public ExternalToolEditingSession(String sessionId, EntityPack<ExternalTool> pack, ExternalToolEditingBean bean)
	{
		super(sessionId, pack, bean);
	}

}
