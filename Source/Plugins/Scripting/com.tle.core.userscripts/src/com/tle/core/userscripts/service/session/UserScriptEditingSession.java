package com.tle.core.userscripts.service.session;

import com.tle.common.EntityPack;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class UserScriptEditingSession extends EntityEditingSessionImpl<UserScriptEditingBean, UserScript>
{
	public UserScriptEditingSession(String sessionId, EntityPack<UserScript> pack, UserScriptEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
